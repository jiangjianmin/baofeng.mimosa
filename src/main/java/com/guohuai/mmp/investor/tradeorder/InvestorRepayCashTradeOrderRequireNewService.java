package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.guohuai.component.web.view.BaseRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.guess.GuessEntity;
import com.guohuai.ams.guess.GuessItemEntity;
import com.guohuai.ams.guess.GuessService;
import com.guohuai.ams.product.OnSaleT0ProductRep;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.investor.InterestFormula;
import com.guohuai.mmp.publisher.investor.InterestRateMethodService;
import com.guohuai.mmp.publisher.investor.InterestReq;
import com.guohuai.mmp.publisher.investor.InterestRequireNew;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.RewardIsNullRep;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class InvestorRepayCashTradeOrderRequireNewService {
	
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;
	@Autowired
	private InterestRequireNew interestRequireNew;
	@Autowired
	private ProductService productService;
	@Autowired
	private GuessService guessService;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private AllocateInterestAuditDao allocateInterestAuditDao;
	@Autowired
	private InterestRateMethodService interestRateMethodService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private InvestorInvestTradeOrderService investorInvestTradeOrderService;
	@Autowired
	private InvestorRedeemInvestTradeOrderService investorRedeemInvestTradeOrderService;
	@Autowired
	private InvestorOpenCycleService investorOpenCycleService;

	@Transactional(value = TxType.REQUIRES_NEW)
	public void processItem(String holdOid, String demandProductOid) {
		PublisherHoldEntity hold = publisherHoldService.findByOid(holdOid);
		/** 解锁分仓可赎回状态 */
		this.investorTradeOrderService.unlockRedeemByHold(hold);
		
		/** 更新合仓可赎回 */
		BigDecimal couponAmount = BigDecimal.ZERO;
		List<InvestorTradeOrderEntity>  orderList = this.investorTradeOrderService.findByPublisherHold(hold);
		for (InvestorTradeOrderEntity orderEntity : orderList) {
			couponAmount = couponAmount.add(orderEntity.getCouponAmount());
		}
		hold.setRedeemableHoldVolume(hold.getLockRedeemHoldVolume().subtract(couponAmount));
		hold.setLockRedeemHoldVolume(BigDecimal.ZERO);
		this.publisherHoldService.saveEntity(hold);
		
		RedeemInvestTradeOrderReq riReq = new RedeemInvestTradeOrderReq();
		riReq.setInvestProductOid(demandProductOid);
		riReq.setOrderAmount(hold.getRedeemableHoldVolume());
		riReq.setRedeemProductOid(hold.getProduct().getOid());
		
		
		investorInvestTradeOrderExtService.cashFailOrder(riReq, hold.getInvestorBaseAccount().getOid());
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void processCashItem(String holdOid, String demandProductOid) {
		PublisherHoldEntity hold = publisherHoldService.findByOid(holdOid);
		/** 解锁分仓可赎回状态 */
		this.investorTradeOrderService.unlockRedeemByHold(hold);
		/** 更新合仓可赎回 */
		hold.setRedeemableHoldVolume(hold.getLockRedeemHoldVolume());
		hold.setLockRedeemHoldVolume(BigDecimal.ZERO);
		this.publisherHoldService.saveEntity(hold);
		
		RedeemInvestTradeOrderReq riReq = new RedeemInvestTradeOrderReq();
		riReq.setInvestProductOid(demandProductOid);
		riReq.setOrderAmount(hold.getRedeemableHoldVolume());
		riReq.setRedeemProductOid(hold.getProduct().getOid());
	
		investorInvestTradeOrderExtService.cashOrder(riReq, hold.getInvestorBaseAccount().getOid());
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void updateExpectedRevenue(String holdOid) {
		PublisherHoldEntity hold = publisherHoldService.findByOid(holdOid);
		BigDecimal holdExpectIncome = BigDecimal.ZERO;
		BigDecimal holdExpectIncomeExt = BigDecimal.ZERO;
		
		List<InvestorTradeOrderEntity>  orderList = this.investorTradeOrderService.findByPublisherHold(hold);
		for (InvestorTradeOrderEntity orderEntity : orderList) {
			BigDecimal expectIncome = InterestFormula.simple(orderEntity.getHoldVolume(), 
					hold.getProduct().getExpAror(), hold.getProduct().getIncomeCalcBasis(), hold.getProduct().getDurationPeriodDays());
			BigDecimal expectIncomeExt = InterestFormula.simple(orderEntity.getHoldVolume(), 
					hold.getProduct().getExpArorSec(), hold.getProduct().getIncomeCalcBasis(), hold.getProduct().getDurationPeriodDays());
			holdExpectIncome = holdExpectIncome.add(expectIncome);
			holdExpectIncomeExt = holdExpectIncomeExt.add(holdExpectIncomeExt);
			orderEntity.setExpectIncome(expectIncome);
			orderEntity.setExpectIncomeExt(expectIncomeExt);
		}
		this.investorTradeOrderService.batchUpdate(orderList);
		
		hold.setExpectIncome(InterestFormula.simple(hold.getTotalVolume(), 
				hold.getProduct().getExpAror(), hold.getProduct().getIncomeCalcBasis(), hold.getProduct().getDurationPeriodDays()));
		hold.setExpectIncomeExt(InterestFormula.simple(hold.getTotalVolume(), 
				hold.getProduct().getExpArorSec(), hold.getProduct().getIncomeCalcBasis(), hold.getProduct().getDurationPeriodDays()));
		this.publisherHoldService.saveEntity(hold);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void allocateIncome(String oid, String auditComment, String productOid, BigDecimal incomeAmount, BigDecimal fpRate, String operator) {
		
		log.info("fpRate : {}, incomeAmount : {}", fpRate, incomeAmount);
		Product product = this.productService.findByOid(productOid);
		if (!Product.STATE_Durationend.equals(product.getState())) {
			// error.define[30054]=募集期尚未结束(CODE:30054)
			// 回滚状态
			this.productService.updateInterestAuditStatus(productOid, Product.INTEREST_AUDIT_STATUS_interesting, Product.INTEREST_AUDIT_STATUS_toAudit);
			throw AMPException.getException(30054);
		}
		//竞猜宝相关校验
		if(product.getGuess()!=null){
			//1.产品对应的竞猜活动开奖后才可派息
			//2.派息利率出现负数或0时不能派息
			GuessEntity guess = product.getGuess();
			List<GuessItemEntity> guessItems = this.guessService.getGuessItemByGuessOid(guess.getOid());
			for(GuessItemEntity item:guessItems){
				BigDecimal percent = item.getPercent();
				if(percent==null){
					// 回滚状态
					this.productService.updateInterestAuditStatus(productOid, Product.INTEREST_AUDIT_STATUS_interesting, Product.INTEREST_AUDIT_STATUS_toAudit);
					throw new GHException("请在派息前先设置开奖结果");
				}
				BigDecimal netPercent = item.getNetPercent();
				if(netPercent!=null&&netPercent.compareTo(BigDecimal.ZERO)<=0){
					// 回滚状态
					this.productService.updateInterestAuditStatus(productOid, Product.INTEREST_AUDIT_STATUS_interesting, Product.INTEREST_AUDIT_STATUS_toAudit);
					throw new GHException("派息利率计算出有负数存在，请重新设置开奖答案");
				}
			}
			
		}
		/** 派息锁 */
		this.productService.repayInterestLock(productOid);
		
		RewardIsNullRep rewardIsNullRep = null;
		if(productIncomeRewardCacheService.hasRewardIncome(product.getOid())) {
			rewardIsNullRep = this.practiceService.rewardIsNotNullRep(product, null);
		} else {
			rewardIsNullRep = this.practiceService.rewardIsNullRep(product, null);
		}
		BigDecimal ratio = DecimalUtil.zoomIn(fpRate, 100);
		incomeAmount = incomeAmount.multiply(new BigDecimal(product.getDurationPeriodDays()));
		InterestReq ireq = new InterestReq();
		ireq.setProduct(product);
		ireq.setTotalInterestedVolume(rewardIsNullRep.getTotalHoldVolume());
		ireq.setIncomeAmount(incomeAmount);
		ireq.setRatio(ratio);
		ireq.setIncomeDate(rewardIsNullRep.getTDate());
		ireq.setIncomeType(IncomeAllocate.ALLOCATE_INCOME_TYPE_durationIncome);
		
		IncomeAllocate incomeAllocate = this.interestRequireNew.newAllocate(ireq);
		
		// 更新派发收益记录
		AllocateInterestAudit allocateInterestAudit = new AllocateInterestAudit();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		int i = this.allocateInterestAuditDao.updateAllocateInterestAudit(oid, operator, AllocateInterestAudit.AUDIT_STATE_AuditPass, now, auditComment);
		if (i < 1) {
			// 派息审核通过出错
			throw AMPException.getException(16005);
		}
		
		this.interestRateMethodService.interest(incomeAllocate.getOid(), incomeAllocate.getProduct().getOid());
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public void processCycleProductCashItem(String holdOid, String demandProductOid) {
		PublisherHoldEntity hold = publisherHoldService.findByOid(holdOid);
		List<InvestorTradeOrderEntity> orderEntities = investorTradeOrderService.findApart(hold.getInvestorBaseAccount(), hold.getProduct());
		if (orderEntities == null || orderEntities.size() != 1) {
			log.error("【循环产品还本付息】用户{}持仓{}下投资订单数据异常！", hold.getInvestorBaseAccount().getOid(), hold.getOid());
			throw new AMPException("持仓" + holdOid + "下投资订单数据异常");
		}
		/** 解锁分仓可赎回状态 */
//		this.investorTradeOrderService.unlockRedeemByHold(hold);
		/** 更新合仓可赎回 */
//		hold.setRedeemableHoldVolume(hold.getLockRedeemHoldVolume());
//		hold.setLockRedeemHoldVolume(BigDecimal.ZERO);
//		this.publisherHoldService.saveEntity(hold);

		RedeemInvestTradeOrderReq riReq = new RedeemInvestTradeOrderReq();
		riReq.setInvestProductOid(demandProductOid);
		riReq.setOrderAmount(hold.getRedeemableHoldVolume());
		riReq.setRedeemProductOid(hold.getProduct().getOid());
		String userOid = hold.getInvestorBaseAccount().getOid();

		/** 创建还本付息、活期申购订单 */
		RedeemTradeOrderReq redeemTradeOrderReq = new RedeemTradeOrderReq();
		redeemTradeOrderReq.setOrderAmount(riReq.getOrderAmount());
		redeemTradeOrderReq.setProductOid(riReq.getRedeemProductOid());
		redeemTradeOrderReq.setUid(userOid);
		InvestorTradeOrderEntity redeemOrder = this.investorRedeemTradeOrderService.createCashTradeOrder(redeemTradeOrderReq);
		investorRedeemTradeOrderService.redeem(redeemOrder);
		InvestorTradeOrderEntity investOrder = investorInvestTradeOrderService.createNoPayInvestTradeOrder(riReq,userOid);
		investOrder.setRelateOid(redeemOrder.getOid());
		investorInvestTradeOrderService.invest(investOrder);

		TradeOrderRep rep = new TradeOrderRep();
		investorRedeemInvestTradeOrderService.redeemInvestNoRequireNewDo(investOrder.getOrderCode(), redeemOrder.getOrderCode());
//		investorInvestTradeOrderService.investThen(rep, investOrder.getOrderCode());
		InvestorTradeOrderEntity investorTradeOrderEntity = orderEntities.get(0);
		investorTradeOrderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_invalidate);
		investorTradeOrderService.saveEntity(investorTradeOrderEntity);
		InvestorOpenCycleRelationEntity openCycleRelationEntity = investorOpenCycleService.findBySourceOrderCode(investorTradeOrderEntity.getOrderCode());
		openCycleRelationEntity.setAssignment(InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_CASH);
		openCycleRelationEntity.setRedeemOrderCode(redeemOrder.getOrderCode());
		openCycleRelationEntity.setRedeemAmount(redeemOrder.getOrderAmount());
		investorOpenCycleService.saveAndFlush(openCycleRelationEntity);

	}
}
