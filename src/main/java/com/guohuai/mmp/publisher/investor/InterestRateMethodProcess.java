package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.ams.product.reward.ProductIncomeRewardService;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.accment.AccParam;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.accment.TradeRequest;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotEntity;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotService;
import com.guohuai.mmp.publisher.investor.holdapartincome.PartIncomeEntity;
import com.guohuai.mmp.publisher.investor.holdapartincome.PartIncomeService;
import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeEntity;
import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeService;
import com.guohuai.mmp.publisher.investor.levelincome.LevelIncomeEntity;
import com.guohuai.mmp.publisher.investor.levelincome.LevelIncomeService;

@Service
public class InterestRateMethodProcess {
	
	Logger logger = LoggerFactory.getLogger(InterestRateMethodProcess.class);
	@Autowired
	private PartIncomeService partIncomeService;
	@Autowired
	private PublisherHoldService holdService;
	@Autowired
	private InvestorIncomeService investorIncomeService;
	@Autowired
	private ProductIncomeRewardService productIncomeRewardService;
	@Autowired
	private LevelIncomeService levelIncomeService;
	@Autowired
	private ProductIncomeRewardCacheService rewardCacheService;
	@Autowired
	private InvestorStatisticsService investorStatisticsService;
	@Autowired
	private ProductService productService;
	@Autowired
	private SnapshotService snapshotService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private AccmentService accmentService;
	
	/**
	 * 
	 * @param product
	 * @param incomeDate
	 * @param hold
	 * @param netUnitAmount
	 * @param fpRate 年化利率 0.05 已经除以100
	 * @return
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void process(Product productIn, Date incomeDate, PublisherHoldEntity hold, BigDecimal netUnitAmount,
			BigDecimal fpRate, InterestRep iRep, IncomeAllocate incomeAllocate, String batchNo) {
		Product product = this.productService.findByOid(productIn.getOid());
		BigDecimal holdIncomeVolume = BigDecimal.ZERO; //合仓利息份额
		BigDecimal holdLockIncomeVolume = BigDecimal.ZERO; //合仓锁定利息份额
		BigDecimal holdIncomeAmount = BigDecimal.ZERO; //合仓利息金额
		BigDecimal holdLockIncomeAmount = BigDecimal.ZERO; //合仓锁定利息金额
		BigDecimal holdBaseAmount = BigDecimal.ZERO; //合仓基础利息金额
		BigDecimal holdRewardAmount = BigDecimal.ZERO; //合仓奖励利息金额
		BigDecimal holdAccuralVolume = BigDecimal.ZERO; //合仓计息份额
		
		InvestorIncomeEntity investorIncomeEntity = new InvestorIncomeEntity(); //发行人-投资人-合仓收益明细
		List<PartIncomeEntity> partIncomeList = new ArrayList<PartIncomeEntity>();
		Map<String, LevelIncomeEntity> rulesLevelMap = new HashMap<String, LevelIncomeEntity>();
		List<SnapshotEntity> snapShotList = snapshotService.findByHoldOidAndSnapShotDate(hold.getOid(), incomeDate);
		if (snapShotList.isEmpty()) {
			return;
		}
		for (SnapshotEntity snapshot : snapShotList) {
			
			
			//奖励收益
			ProductIncomeReward reward = rewardCacheService.getRewardEntity(product.getOid(), snapshot.getHoldDays());
			BigDecimal rewardAmount = BigDecimal.ZERO;
			
			
			LevelIncomeEntity levelIncomeEntity = null;
			if (null != reward) { //奖励金额
				rewardAmount = productIncomeRewardService.getRewardAmount(reward, snapshot.getSnapshotVolume());
			}
			
			//<<本金>>利息金额
			BigDecimal baseAmount = null;
			
			baseAmount = caclDayInterest(fpRate, product, snapshot, incomeAllocate);
			logger.info("baseAmount={}, rewardAmount={}", baseAmount, rewardAmount);
			
			//<<本金+奖励>>利息金额
			BigDecimal incomeAmount = baseAmount.add(rewardAmount);
			//<<本金+奖励>>利息份额
			BigDecimal incomeVolume = incomeAmount.divide(netUnitAmount, DecimalUtil.scale, DecimalUtil.roundMode);
			
			if (null != reward) {
				//创建<<发行人-投资人-阶段收益明细>>
				levelIncomeEntity = rulesLevelMap.get(reward.getOid());
				
				if (null != levelIncomeEntity) {
					levelIncomeEntity.setIncomeAmount(levelIncomeEntity.getIncomeAmount().add(incomeAmount)); //收益金额
					levelIncomeEntity.setBaseAmount(levelIncomeEntity.getBaseAmount().add(baseAmount));
					levelIncomeEntity.setRewardAmount(levelIncomeEntity.getRewardAmount().add(rewardAmount));
					levelIncomeEntity.setAccureVolume(levelIncomeEntity.getAccureVolume().add(snapshot.getSnapshotVolume())); //计息份额
					levelIncomeEntity.setValue(levelIncomeEntity.getValue().add(snapshot.getSnapshotVolume()).add(incomeAmount)); //市值
				} else {
					levelIncomeEntity = new LevelIncomeEntity();
					levelIncomeEntity.setPublisherHold(hold); //所属持有人手册
					levelIncomeEntity.setProduct(product); //所属产品
					levelIncomeEntity.setReward(reward); //所属奖励规则
					levelIncomeEntity.setInvestorBaseAccount(snapshot.getInvestorBaseAccount()); //所属投资人
					levelIncomeEntity.setInvestorIncome(investorIncomeEntity); //所属合仓明细
					levelIncomeEntity.setIncomeAmount(incomeAmount); //收益金额
					levelIncomeEntity.setBaseAmount(baseAmount);
					levelIncomeEntity.setRewardAmount(rewardAmount);
					levelIncomeEntity.setAccureVolume(snapshot.getSnapshotVolume()); //计息份额
					levelIncomeEntity.setValue(snapshot.getSnapshotVolume().add(incomeAmount));
					levelIncomeEntity.setConfirmDate(incomeDate); //确认日期
					rulesLevelMap.put(reward.getOid(), levelIncomeEntity);
				}
			}
			
			//更新<<发行人-持有人分仓>>
			if (Product.TYPE_Producttype_02.equals(product.getType().getOid())) {
				investorTradeOrderService.updateHoldApart4Interest(snapshot.getOrderOid(), incomeVolume, incomeAmount, 
						netUnitAmount, incomeDate, baseAmount, rewardAmount);
			} else {
				investorTradeOrderService.updateHoldApart4InterestTn(snapshot.getOrderOid(), incomeAmount, incomeDate);
			}
			//更新后续计息快照
			if (BigDecimal.ZERO.compareTo(incomeVolume) != 0) {
				snapshotService.increaseSnapshotVolume(snapshot.getOrderOid(), incomeVolume, incomeDate);
			}
			
			//创建<<发行人-投资人-分仓收益明细>>
			PartIncomeEntity partIncomeEntity = new PartIncomeEntity();
			partIncomeEntity.setPublisherHold(hold); //所属持有人手册
			partIncomeEntity.setProduct(product); //所属产品
			partIncomeEntity.setIncomeAllocate(incomeAllocate);
			partIncomeEntity.setInvestorBaseAccount(snapshot.getInvestorBaseAccount()); //所属投资人
			//partIncomeEntity.setOrder(snapshot.getOrder()); //所属分仓
			partIncomeEntity.setHoldIncome(investorIncomeEntity); //所属合仓收益
			partIncomeEntity.setReward(reward); //所属奖励规则
			partIncomeEntity.setLevelIncome(levelIncomeEntity); //所属阶段收益
			partIncomeEntity.setIncomeAmount(incomeAmount); //收益金额
			partIncomeEntity.setBaseAmount(baseAmount);
			partIncomeEntity.setRewardAmount(rewardAmount); //奖励金额
			partIncomeEntity.setAccureVolume(snapshot.getSnapshotVolume()); //计息份额
			partIncomeEntity.setConfirmDate(incomeDate); //确认日期
			partIncomeList.add(partIncomeEntity);
			
			//累计合仓
			if (InvestorTradeOrderEntity.TRADEORDER_redeemStatus_yes.equals(snapshot.getRedeemStatus())) {
				holdIncomeVolume = holdIncomeVolume.add(incomeVolume);
				holdIncomeAmount = holdIncomeAmount.add(incomeAmount);
			} else {
				holdLockIncomeVolume = holdLockIncomeVolume.add(incomeVolume);
				holdLockIncomeAmount = holdLockIncomeAmount.add(incomeAmount);
			}
			
			holdAccuralVolume = holdAccuralVolume.add(snapshot.getSnapshotVolume());
			holdBaseAmount = holdBaseAmount.add(baseAmount);
			holdRewardAmount = holdRewardAmount.add(rewardAmount);
		}
		//更新<<发行人-持有人手册>>
		if (Product.TYPE_Producttype_02.equals(product.getType().getOid())) {
			//活期
			holdService.updateHold4Interest(hold.getOid(), holdIncomeVolume, holdIncomeAmount,
					holdLockIncomeVolume, holdLockIncomeAmount, netUnitAmount, incomeDate, holdBaseAmount, holdRewardAmount);
			
//			cacheHoldService.syncHoldWhenInterest(hold, holdIncomeVolume, holdIncomeAmount,
//					holdLockIncomeVolume, holdLockIncomeAmount, netUnitAmount, incomeDate, holdBaseAmount, holdRewardAmount, batchNo);
		} else {
			holdService.updateHold4InterestTn(hold.getOid(), holdIncomeAmount, holdLockIncomeAmount, incomeDate);
//			cacheHoldService.syncTnHoldWhenInterest(hold, holdIncomeAmount, holdLockIncomeAmount, incomeDate, batchNo);
		}
		
		//创建<<发行人-投资人-合仓收益明细>>
		investorIncomeEntity.setPublisherHold(hold); //所属持有人手册
		investorIncomeEntity.setIncomeAllocate(incomeAllocate);
		investorIncomeEntity.setProduct(product); //所属产品
		investorIncomeEntity.setInvestorBaseAccount(hold.getInvestorBaseAccount()); //所属投资人
		investorIncomeEntity.setIncomeAmount(holdIncomeAmount.add(holdLockIncomeAmount));
		investorIncomeEntity.setBaseAmount(holdBaseAmount);
		investorIncomeEntity.setRewardAmount(holdRewardAmount);
		investorIncomeEntity.setAccureVolume(holdAccuralVolume);
		investorIncomeEntity.setConfirmDate(incomeDate);
		investorIncomeService.saveEntity(investorIncomeEntity);
		
		//保存<<发行人-投资人-阶段收益明细>>
		levelIncomeService.saveBatch(rulesLevelMap.values());
		
		//保存<<发行人-投资人-分仓收益明细>>
		partIncomeService.saveBatch(partIncomeList);
		
		if (Product.TYPE_Producttype_02.equals(product.getType().getOid())) {
			investorStatisticsService.interestStatistics(hold.getInvestorBaseAccount(), holdIncomeAmount, holdLockIncomeAmount, incomeDate);
			
		} else {
			investorStatisticsService.interestStatisticsTn(hold.getInvestorBaseAccount(), holdIncomeAmount, holdLockIncomeAmount, incomeDate);
			
		}
		
		
		iRep.setAmount(holdIncomeAmount.add(holdLockIncomeAmount));
		iRep.setBaseAmount(holdBaseAmount);
		iRep.setRewardAmount(holdRewardAmount);
		
		TradeRequest ireq = new TradeRequest();
		ireq.setUserOid(hold.getInvestorBaseAccount().getMemberId());
		ireq.setUserType(AccParam.UserType.INVESTOR.toString());
		ireq.setOrderType(AccParam.OrderType.INTEREST.toString());
		ireq.setProductType(this.accmentService.getProductType(product));
		ireq.setRelationProductNo(product.getOid());
		ireq.setBalance(iRep.getAmount());
		ireq.setRemark("interest");
		ireq.setOrderNo(StringUtil.uuid());
		accmentService.trade(ireq);
	}
	
	public  BigDecimal caclDayInterest(BigDecimal fpRate, Product product,
			SnapshotEntity snapshotEntity, IncomeAllocate incomeAllocate) {
		if (IncomeAllocate.ALLOCATE_INCOME_TYPE_durationIncome.equals(incomeAllocate.getAllocateIncomeType()) &&
				Product.TYPE_Producttype_01.equals(product.getType().getOid())) {
			
			return InterestFormula.simple(snapshotEntity.getSnapshotVolume(), fpRate, product.getIncomeCalcBasis(), product.getDurationPeriodDays());
			
		} else {
			return InterestFormula.compound(snapshotEntity.getSnapshotVolume(), fpRate, product.getIncomeCalcBasis());
		}
	}

}
