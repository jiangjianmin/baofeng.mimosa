package com.guohuai.mmp.publisher.holdapart.snapshot;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeDao;
import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.StaticProperties;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class SnapshotService {

	@Autowired
	private SnapshotDao snapshotDao;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private ProductService productService;
	@Autowired
	private ProductLabelService productLabelService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private SnapshotServiceRequiresNew snapshotServiceRequiresNew;
	@Autowired
	private PracticeService practiceService;
	@Autowired
    private InvestorIncomeDao investorIncomeDao;

	public void snapshot() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_snapshot)) {
			this.snapshotDo();
		}
	}

	/**
	 * 计息份额快照
	 */
	public void snapshotDo() {
		
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_snapshot);
		
		try {
			Date curDate = StaticProperties.isIs24() ? DateUtil.getBeforeDate() : DateUtil.getSqlDate();
			/**
			 * 活期产品 募集期、清盘中 发放收益
			 */
			List<Product> productList = productService.findProductT04Snapshot();
			for (Product product : productList) {
				String productLabel = productLabelService.findLabelByProduct(product);
				if (labelService.isProductLabelHasAppointLabel(productLabel, LabelEnum.tiyanjin.toString())) {
					investorTradeOrderService.snapshotTasteCouponVolume(product, curDate);
				} else {
					investorTradeOrderService.snapshotVolume(product, curDate);
				}
			}
			/**
			 * 定期产品 募集期、募集结束在募集失败和募集成立之前发放收益
			 */
			productList = productService.findProductTn4Snapshot();
			for (Product product : productList) {
				investorTradeOrderService.snapshotVolume(product, curDate);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_snapshot);
	}

	
	public SnapshotEntity findByOrderAndSnapShotDate(String orderOid, Date incomeDate) {
		SnapshotEntity entity = this.snapshotDao.findByOrderAndSnapShotDate(orderOid, incomeDate);

		return entity;
	}

	public int increaseSnapshotVolume(String orderOid, BigDecimal holdIncomeVolume, Date incomeDate) {

		return this.snapshotDao.increaseSnapshotVolume(orderOid, holdIncomeVolume, incomeDate);

	}

	public List<SnapshotEntity> findByHoldOidAndSnapShotDate(String holdOid, Date incomeDate) {
		return this.snapshotDao.findByHoldOidAndSnapShotDate(holdOid, incomeDate);
	}
	
	public List<SnapshotEntity> findByInvestorOidAndProductOidAndSnapShotDate(String investorOid, String productOid, Date incomeDate) {
		return this.snapshotDao.findByInvestorOidAndProductOidAndSnapShotDate(investorOid, productOid, incomeDate);
	}
	
	/**
	 * 复利无奖励收益
	 * @param productOid
	 * @param baseIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestCompoundWithoutRewardIncome(String productOid, BigDecimal baseIncomeRatio, Date incomeDate){
		int result=this.snapshotDao.distributeInterestCompoundWithoutRewardIncome(productOid, baseIncomeRatio, incomeDate);
		log.info("结束复利无奖励收益，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 复利有奖励收益
	 * @param productOid
	 * @param baseIncomeRatio
	 * @param rewardIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestCompoundWithRewardIncome(String productOid, BigDecimal baseIncomeRatio,Date incomeDate){
		int result= this.snapshotDao.distributeInterestCompoundWithRewardIncome(productOid, baseIncomeRatio, incomeDate);
		log.info("结束复利有奖励收益，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 一次性付息无奖励收益
	 * @param productOid
	 * @param baseIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestSingleWithoutRewardIncome(String productOid, BigDecimal baseIncomeRatio, Date incomeDate,int holdDays,int calcBaseDays){
		int result= this.snapshotDao.distributeInterestSingleWithoutRewardIncome(productOid, baseIncomeRatio, incomeDate, holdDays, calcBaseDays);
		log.info("结束一次性付息无奖励收益，结果为{}",result>0);
		return result;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: distributeTnProductInterestIncomeAgain
	 * @Description: 二次计算定期收益
	 * @param productOid
	 * @param incomeDate
	 * @return int
	 * @date 2017年6月12日 下午5:49:41
	 * @since  1.0.0
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeTnProductInterestIncomeAgain(String productOid, Date incomeDate) {
		int result= this.snapshotDao.distributeTnProductInterestIncomeAgain(productOid, incomeDate);
		log.info("二次计算定期收益，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 一次性付息有奖励收益
	 * @param productOid
	 * @param baseIncomeRatio
	 * @param rewardIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestSingleWithRewardIncome(String productOid, BigDecimal baseIncomeRatio, Date incomeDate,int holdDays,int calcBaseDays){
		int result= this.snapshotDao.distributeInterestSingleWithRewardIncome(productOid, baseIncomeRatio, incomeDate, holdDays, calcBaseDays);
		log.info("结束一次性付息有奖励收益，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 根据订单表分发收益
	 * @param productOid
	 * @param incomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeOrderInterest(String productOid,Date incomeDate,BigDecimal netUnitShare){
		int result= this.snapshotDao.distributeOrderInterest(productOid, incomeDate, netUnitShare);
		log.info("结束根据订单表分发收益，结果为{}",result>0);
		return result;
	}
	
	
	/**
	 * 收益更新到产品表
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestToProduct(String productOid,Date incomeDate){
		int result= this.snapshotDao.distributeInterestToProduct(productOid, incomeDate);
		log.info("结束收益更新到产品表，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 发放收益到投资者收益明细（订单粒度）
	 * @param productOid
	 * @param incomeDate
	 * @param incomeOid
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestToInvestorIncome(String productOid,Date incomeDate,String incomeOid){
		int result=0;
		if (this.snapshotDao.hasDistributedInterestToInvestorIncome(productOid, incomeDate)==0) {
			result= this.snapshotDao.distributeInterestToInvestorIncome(productOid, incomeDate, incomeOid);
		}
		log.info("结束放收益到投资者收益明细（订单粒度），结果为{}",result>0);
		return result;
	}
	
	/**
	 * 发放收益到投资者阶梯奖励收益明细(投资者阶梯奖励收益粒度)
	 * @param productOid
	 * @param incomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestToInvestorLevelIncome(String productOid,Date incomeDate,BigDecimal netUnitShare){
		int result=0;
		if (this.snapshotDao.hasDistributedInterestToInvestorLevelIncome(productOid, incomeDate)==0) {
			result= this.snapshotDao.distributeInterestToInvestorLevelIncome(productOid, incomeDate, netUnitShare);
		}
		log.info("结束发放收益到投资者阶梯奖励收益明细(投资者阶梯奖励收益粒度)，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 发放收益到投资者合仓收益明细（投资者合仓粒度）
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestToInvestorHoldIncome(String productOid,Date incomeDate){
		int result=0;
		if (this.snapshotDao.hasDistributedInterestToInvestorHoldIncome(productOid, incomeDate) ==0) {
			result= this.snapshotDao.distributeInterestToInvestorHoldIncome(productOid, incomeDate);
		}
		log.info("结束发放收益到投资者合仓收益明细（投资者合仓粒度），结果为{}",result>0);
		return result;
	}
	
	/**
	 * 再次更新投资者收益明细的引用holdIncomeOid，levelIncomeOid
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int reupdateInvestorIncomeWithRewardIncome(String productOid,Date incomeDate){
		int result=0;
		if (this.snapshotDao.hasReupdatedInvestorIncomeWithRewardIncome(productOid, incomeDate) == 0) {
			result= this.snapshotDao.reupdateInvestorIncomeWithRewardIncome(productOid, incomeDate);
		}
		log.info("结束再次更新投资者收益明细的引用holdIncomeOid，levelIncomeOid，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 再次更新投资者收益明细的引用holdIncomeOid
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int reupdateInvestorIncomeWithoutRewardIncome(String productOid,Date incomeDate){
		int result=0;
		if (this.snapshotDao.hasReupdatedInvestorIncomeWithoutRewardIncome(productOid, incomeDate) == 0) {
			result= this.snapshotDao.reupdateInvestorIncomeWithoutRewardIncome(productOid, incomeDate);
		}
		log.info("结束再次更新投资者收益明细的引用holdIncomeOid，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 根据持有人手册表分发收益
	 * @param productOid
	 * @param incomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeHoldInterest(String productOid,Date incomeDate,BigDecimal netUnitShare,String productType){
		int result=0;
		if (this.snapshotDao.hasdistributedHoldInterest(productOid, incomeDate)==0) {
			result= this.snapshotDao.distributeHoldInterest(productOid, incomeDate, netUnitShare);
		}
		if(result>0){//持有人手册收益更新成功后
			// 收益更新到投资统计信息
			this.snapshotServiceRequiresNew.distributeInterestToInvestorStatistic(productOid, incomeDate, productType);
			//更新在指定收益日期后已经拍过快照的快照数据
			this.reupdateAfterIncomeDateAllSnapshot(productOid, incomeDate, netUnitShare);
		}
		log.info("结束根据持有人手册表分发收益，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 试算有奖励收益
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int practiceDistributeInterestWithRewardIncome(String productOid,Date incomeDate){
		return this.snapshotDao.practiceDistributeInterestWithRewardIncome(productOid, incomeDate);
	}
	
	/**
	 * 试算无奖励收益
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int practiceDistributeInterestWithoutRewardIncome(String productOid,Date incomeDate){
		return this.snapshotDao.practiceDistributeInterestWithoutRewardIncome(productOid, incomeDate);
	}
	
	/**
	 * 获取已分派收益信息
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public Object[] getDistributedInterestInfo(String productOid,Date incomeDate){
		return this.snapshotDao.getDistributedInterestInfo(productOid, incomeDate).get(0);
	}
	
	/**
	 * 将快照表中的计算完收益的数据以投资者维度插入临时表中
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int insertIntoSnapshotTmp(String productOid, Date incomeDate){
		this.snapshotDao.truncateSnapshotTmp();
		int result= this.snapshotDao.insertIntoSnapshotTmp(productOid, incomeDate);
		log.info("将快照表中的计算完收益的数据以投资者维度插入临时表中，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 重新同步在派发收益日期之后已经拍过快照的全部数据
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	public void reupdateAfterIncomeDateAllSnapshot(String productOid, Date incomeDate,
			BigDecimal netUnitShare){
		List<Date> snapshotDates=this.snapshotDao.getAfterIncomeDate(productOid, incomeDate);
		for (Date date : snapshotDates) {
			this.snapshotServiceRequiresNew.reupdateAfterIncomeDateSnapshot(productOid, incomeDate, date, netUnitShare);
			this.practiceService.processOneItem(productOid, date);
		}
	}
	/**体验金发放收益到订单表
	 * @param productOid
	 * @param incomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeOrderInterestForTasteCoupon(String productOid, Date incomeDate, BigDecimal netUnitShare) {
		int result= this.snapshotDao.distributeOrderInterestForTasteCoupon(productOid, incomeDate, netUnitShare);
		log.info("结束根据订单表分发体验金收益，结果为{}",result>0);
		return result;
	}

	/**体验金复利无奖励收益
	 * @param oid
	 * @param baseIncomeRatio
	 * @param incomeDate
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestCompoundWithoutRewardIncomeForTasteCoupon(String oid, BigDecimal baseIncomeRatio,
			Date incomeDate) {
		int result=this.snapshotDao.distributeInterestCompoundWithoutRewardIncomeForTasteCoupon(oid, baseIncomeRatio, incomeDate);
		log.info("结束复利无奖励收益，结果为{}",result>0);
		return result;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: checkTnRaiseIncome
	 * @Description: 检查募集期收益明细中是否已经生成过募集期收益发放明细
	 * @param product
	 * @return boolean
	 * @date 2017年6月12日 下午4:45:24
	 * @since  1.0.0
	 */
	public boolean checkTnRaiseIncome(Product product) {
		int result = this.snapshotDao.queryTnRaiseIncomeCount(product.getOid());
		return result>0;
	}
	/**关联竞猜活动的产品收益分配
	 * @param productOid
	 * @param fpRate 
	 * @param incomeDate
	 * @param holdDays
	 * @param calcBaseDays
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestSingleWithoutRewardIncomeForInvestItem(String productOid, BigDecimal fpRate, Date incomeDate,int holdDays,int calcBaseDays) {
		int result= this.snapshotDao.distributeInterestSingleWithoutRewardIncomeForInvestItem(productOid,fpRate, incomeDate, holdDays, calcBaseDays);
		log.info("结束一次性付息无奖励收益，结果为{}",result>0);
		return result;
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: updateTnRaisingIncome
	 * @Description: 更新定期募集期收益发放明细（再次发放）
	 * @param product
	 * @return int
	 * @date 2017年6月12日 下午4:01:25
	 * @since  1.0.0
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int updateTnRaisingIncome(Product product) {
		int result = this.snapshotDao.updateTnRaisingIncome(product.getOid());
		log.info("更新定期募集期收益发放明细，结果为{}", result>0);
		return result;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: initTnRaisingIncome
	 * @Description: 首次生成定期存续期收益发放明细
	 * @param product
	 * @param incomeDate
	 * @return int
	 * @date 2017年6月12日 下午4:01:54
	 * @since  1.0.0
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int initTnRaisingIncome(Product product, Date incomeDate) {
		int result = this.snapshotDao.initTnRaisingIncome(product.getOid(), incomeDate);
		log.info("首次生成定期存续期收益发放明细，结果为{}", result>0);
		return result;
	}

	/**
	 * 循环开放产品发放收益到投资者合仓收益明细
	 * @return
	 */
	public int distributeCycleProductInterestToInvestorHoldIncome(){
		int result= this.snapshotDao.distributeCycleProductInterestToInvestorHoldIncome();
		log.info("【循环开放产品】发放收益到投资者合仓收益明细，结果为{}",result>0);
		return result;
	}

	/**
	 * 循环开放产品发放收益到投资者收益明细
	 * @return
	 */
	public int distributeCycleProductInterestToInvestorIncome() {
		int result = this.snapshotDao.distributeCycleProductInterestToInvestorIncome();
		log.info("【循环开放产品】发放收益到投资者收益明细，结果为{}",result>0);
		return result;
	}

	/**
	 * 循环开放产品发放收益到投资者统计信息表
	 * @return
	 */
    public int distributeCycleProductInterestToStatisticsIncome() {
		int result = this.snapshotDao.distributeCycleProductInterestToStatisticsIncome();
		log.info("【循环开放产品】发放收益到投资者统计信息表，结果为{}",result>0);
		return result;
    }

    public int distributeCycleProductInterest(String holdOid, String productOid, String investorOid, BigDecimal totalIncome, BigDecimal orderAmount) {
        return snapshotDao.distributeCycleProductInterest(holdOid, productOid, investorOid, totalIncome, orderAmount);
    }

    public int updateCycleProductInterest(String oid, BigDecimal totalIncome, BigDecimal orderAmount){
        return snapshotDao.updateCycleProductInterest(oid, totalIncome, orderAmount);
    }

    public InvestorIncomeEntity getCycleProductInterest(String productOid, String investorOid, Date confirmDate){
        return investorIncomeDao.getCycleProductInterest(productOid, investorOid, confirmDate);
    }

    public int distributeCycleProductInterestIncome(String redeemOrderOid, BigDecimal orderAmount){
        return snapshotDao.distributeCycleProductInterestIncome(redeemOrderOid, orderAmount);
    }
}
