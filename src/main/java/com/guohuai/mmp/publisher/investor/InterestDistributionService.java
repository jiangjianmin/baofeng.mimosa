package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.duration.fact.income.IncomeAllocateDao;
import com.guohuai.ams.guess.GuessService;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotService;
import com.guohuai.mmp.publisher.investor.interest.result.InterestResultEntity;
import com.guohuai.mmp.publisher.investor.interest.result.InterestResultService;
import com.guohuai.mmp.sys.SysConstant;

import lombok.extern.slf4j.Slf4j;
/**
 * 
 * @author jeffrey
 *派息优化处理
 */
@Slf4j
@Service
public class InterestDistributionService {
	@Autowired
	SnapshotService snapshotService;
	@Autowired
	private IncomeAllocateDao incomeAllocateDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	@Autowired
	private InterestResultService interestResultService;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private ProductLabelService productLabelService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private GuessService guessService;

	/**
	 * 派息
	 * 
	 * @param incomeOid
	 *            收益分派主键ID
	 * @param productOid
	 *            产品主键ID
	 */
	public void distributeInterestByProduct(String incomeOid, String productOid) {
		long beginTime = System.currentTimeMillis();
		IncomeAllocate incomeAllocate = incomeAllocateDao.findOne(incomeOid);
		Product product = productService.findByOid(productOid);
		Date incomeDate = incomeAllocate.getBaseDate();
		BigDecimal fpRate = incomeAllocate.getRatio();
		BigDecimal baseIncomeRatio = InterestFormula.caclDayInterest(fpRate,
				Integer.parseInt(product.getIncomeCalcBasis()));// 复利，基础万份收益
		BigDecimal netUnitShare = product.getNetUnitShare();
		
		log.info("begin to handle distributeInterestByProduct:incomeOid={},productOid={},incomeDate={}",incomeOid,productOid,incomeDate);
		if(!this.checkPracticeAmount(product, incomeDate, incomeAllocate)){//如果试算规模为0，不进行收益发放
			return;
		}

		// 1.在快照表中更新要发放的收益
		this.caclInterest(product, incomeDate, incomeAllocate, baseIncomeRatio,fpRate);
		if (Product.TYPE_Producttype_01.equals(product.getType().getOid())
				&& IncomeAllocate.ALLOCATE_INCOME_TYPE_raiseIncome.equals(incomeAllocate.getAllocateIncomeType())
				&& product.getRecPeriodExpAnYield().compareTo(SysConstant.BIGDECIMAL_defaultValue) > 0) {// 定期--生成定期募集期收益发放明细--yihonglei 20170612 增加定期募集期收益拆分分支判断
			this.distributeTnRaiseIncome(product, incomeDate);
		} else {
			//判断体验金产品
			String productLabel = productLabelService.findLabelByProduct(product);
			if (labelService.isProductLabelHasAppointLabel(productLabel, LabelEnum.tiyanjin.toString())) {
				//2.体验金根据订单表分发收益
				this.snapshotService.distributeOrderInterestForTasteCoupon(productOid, incomeDate, netUnitShare);
			} else {
				// 2.根据订单表分发收益
				this.snapshotService.distributeOrderInterest(productOid, incomeDate, netUnitShare);
			}
			// 3.根据持有人手册分发收益
			this.snapshotService.distributeHoldInterest(productOid, incomeDate, netUnitShare,product.getType().getOid());
			// 4.收益更新到产品表，在send接口已经处理了产品的currentVolume
	//		this.snapshotService.distributeInterestToProduct(productOid, incomeDate);
			// 5.发放收益到投资者收益明细（订单粒度）
			this.snapshotService.distributeInterestToInvestorIncome(productOid, incomeDate, incomeOid);
			if (this.productIncomeRewardCacheService.hasRewardIncome(productOid)) {// 有定义奖励收益
				// 6.发放收益到投资者阶梯奖励收益明细(投资者阶梯奖励收益粒度)
				this.snapshotService.distributeInterestToInvestorLevelIncome(productOid, incomeDate, netUnitShare);
			}
			// 7.发放收益到投资者合仓收益明细（投资者合仓粒度）
			this.snapshotService.distributeInterestToInvestorHoldIncome(productOid, incomeDate);
			// 8.再次更新投资者收益明细的引用
			if (this.productIncomeRewardCacheService.hasRewardIncome(productOid)) {// 有定义奖励收益
				// 再次更新投资者收益明细的引用holdIncomeOid，levelIncomeOid
				this.snapshotService.reupdateInvestorIncomeWithRewardIncome(productOid, incomeDate);
			} else {
				// 再次更新投资者收益明细的引用holdIncomeOid
				this.snapshotService.reupdateInvestorIncomeWithoutRewardIncome(productOid, incomeDate);
			}
	
			//9.更新已发放收益数据
			this.updateDistributedInterest(product, incomeDate, incomeAllocate);
		}
		log.info("发放产品oid={},执行总时间：{}秒", productOid, (System.currentTimeMillis() - beginTime) / 1000);
	}

	/**
	 * 基于快照表算出收益
	 * 
	 * @param product
	 * @param incomeDate
	 * @param incomeAllocate
	 * @param baseIncomeRatio
	 * @param fpRate
	 */
	private void caclInterest(Product product, Date incomeDate, IncomeAllocate incomeAllocate,
			BigDecimal baseIncomeRatio,BigDecimal fpRate) {
		if (Product.TYPE_Producttype_01.equals(product.getType().getOid())
				&& IncomeAllocate.ALLOCATE_INCOME_TYPE_durationIncome.equals(incomeAllocate.getAllocateIncomeType())) {// 定期
																														// 存续期收益
			if (this.productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {// 有定义奖励收益
				// 单利有奖励收益
				this.snapshotService.distributeInterestSingleWithRewardIncome(product.getOid(), fpRate,
						incomeDate, product.getDurationPeriodDays(), Integer.parseInt(product.getIncomeCalcBasis()));
			} else {
				
				if(guessService.hasGuessWithProduct(product)){//判断是否关联竞猜活动
					//关联上竞猜活动
					this.snapshotService.distributeInterestSingleWithoutRewardIncomeForInvestItem(product.getOid(),fpRate,
					incomeDate, product.getDurationPeriodDays(), Integer.parseInt(product.getIncomeCalcBasis()));
				}else{
					this.snapshotService.distributeInterestSingleWithoutRewardIncome(product.getOid(), fpRate,
							incomeDate, product.getDurationPeriodDays(), Integer.parseInt(product.getIncomeCalcBasis()));
				}
			}
			// ------------------yihonglei 20170612 add start---------------------
			/*
			 * 定期募集期如果有收益，定期订单最后收益 = 定期存续期订单利息+定期募集期订单利息
			 */
			if (product.getRecPeriodExpAnYield().compareTo(SysConstant.BIGDECIMAL_defaultValue) > 0) {
				log.info("二次计算定期收益开始,productOid:{},recPeriodExpAnYield:{}", product.getOid(), product.getRecPeriodExpAnYield());
				this.snapshotService.distributeTnProductInterestIncomeAgain(product.getOid(), incomeDate);// 二次计算定期收益
				log.info("二次计算定期收益结束!");
			}
			// ------------------yihonglei 20170612 add end-----------------------
		} else {
			// 复利模式
			if (this.productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {// 有定义奖励收益
				this.snapshotService.distributeInterestCompoundWithRewardIncome(product.getOid(), baseIncomeRatio,
						incomeDate);
			} else {
				//判断体验金产品
				String productLabel = productLabelService.findLabelByProduct(product);
				if (labelService.isProductLabelHasAppointLabel(productLabel, LabelEnum.tiyanjin.toString())) {
					// 体验金复利无奖励收益
					this.snapshotService.distributeInterestCompoundWithoutRewardIncomeForTasteCoupon(product.getOid(), baseIncomeRatio,
							incomeDate);
				} else {
					// 复利无奖励收益
					this.snapshotService.distributeInterestCompoundWithoutRewardIncome(product.getOid(), baseIncomeRatio,
							incomeDate);
				}
				
			}
		}
		//插入快照收益数据到临时表
		this.snapshotService.insertIntoSnapshotTmp(product.getOid(), incomeDate);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: distributeTnRaiseIncome
	 * @Description:生成定期募集期收益发放明细
	 * @return void
	 * @date 2017年6月12日 上午11:55:27
	 * @since  1.0.0
	 */
	public void distributeTnRaiseIncome(Product product, Date incomeDate) {
		log.info("<------生成定期募集期收益发放明细开始!募集期年化收益率:{}------>"+product.getRecPeriodExpAnYield());
		if (this.snapshotService.checkTnRaiseIncome(product)) {// 检查募集期收益明细中是否已经生成过募集期收益发放明细
			// 更新定期募集期收益发放明细（再次发放）
			this.snapshotService.updateTnRaisingIncome(product);
		}
		
		// 首次生成定期募集期收益发放明细
		this.snapshotService.initTnRaisingIncome(product,incomeDate);
		log.info("<------生成定期募集期收益发放明细结束!------>");
	}
	
	/**
	 * 试算规模是否大于0
	 * @param product
	 * @param incomeDate
	 * @param incomeAllocate
	 * @return
	 */
	private boolean checkPracticeAmount(Product product, Date incomeDate, IncomeAllocate incomeAllocate){
		BigDecimal fpBaseAmount = incomeAllocate.getLeftAllocateBaseIncome();
		BigDecimal fpRewardAmount = incomeAllocate.getLeftAllocateRewardIncome();
		BigDecimal fpAmount = fpBaseAmount.add(fpRewardAmount);
		
		BigDecimal totalVolume = incomeAllocate.getCapital();
		//待计息份额 = 本金份额 + 最后计息基数
		if (null == totalVolume || SysConstant.BIGDECIMAL_defaultValue.compareTo(totalVolume) == 0) {
			//收益分配日志
			InterestResultEntity result = interestResultService.createEntity(product, incomeAllocate, incomeDate);
			log.info("checkPracticeAmount,{}待计息从份额为零", product.getCode());
			result.setLeftAllocateIncome(fpAmount);
			result.setStatus(InterestResultEntity.RESULT_status_ALLOCATED);
			result.setAnno("待计息份额为零");
			this.interestResultService.saveEntity(result);
			interestResultService.send(result);
			return false;
		}
		return true;
	}
	
	


	/**
	 * 更新已发放收益数据
	 * 
	 * @param product
	 * @param incomeDate
	 * @param incomeAllocate
	 */
	private void updateDistributedInterest(Product product, Date incomeDate, IncomeAllocate incomeAllocate) {
		BigDecimal fpBaseAmount = incomeAllocate.getLeftAllocateBaseIncome();
		BigDecimal fpRewardAmount = incomeAllocate.getLeftAllocateRewardIncome();
		BigDecimal fpAmount = fpBaseAmount.add(fpRewardAmount);
		InterestResultEntity result = interestResultService.createEntity(product, incomeAllocate, incomeDate);
		Object[] distributedInterest = this.snapshotService.getDistributedInterestInfo(product.getOid(), incomeDate);
		result.setSuccessAllocateIncome(new BigDecimal(distributedInterest[0].toString()));
		result.setSuccessAllocateRewardIncome(new BigDecimal(distributedInterest[1].toString()));
		result.setSuccessAllocateBaseIncome(new BigDecimal(distributedInterest[2].toString()));
		result.setSuccessAllocateInvestors(Integer.parseInt(distributedInterest[3].toString()));
		result.setLeftAllocateIncome(fpAmount.subtract(result.getSuccessAllocateIncome()));
		result.setLeftAllocateBaseIncome(fpBaseAmount.subtract(result.getSuccessAllocateBaseIncome()));
		result.setLeftAllocateRewardIncome(fpRewardAmount.subtract(result.getSuccessAllocateRewardIncome()));
		if (result.getSuccessAllocateInvestors() > 0) {
			result.setStatus(InterestResultEntity.RESULT_status_ALLOCATED);
		} else {
			result.setStatus(InterestResultEntity.RESULT_status_ALLOCATEFAIL);
		}
		/** 发送计息结果 */
		this.interestResultService.saveEntity(result);
		interestResultService.send(result);

		/** 发行人统计 */
		publisherStatisticsService.increaseTotalInterestAmount(product.getPublisherBaseAccount(),
				result.getSuccessAllocateIncome());

		/** 平台统计 */
		this.platformStatisticsService.updateStatistics4TotalInterestAmount(result.getSuccessAllocateIncome());
	}

}
