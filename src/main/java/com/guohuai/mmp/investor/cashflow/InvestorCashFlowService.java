package com.guohuai.mmp.investor.cashflow;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.bankorder.InvestorBankOrderEntity;
import com.guohuai.mmp.investor.bankorder.InvestorBankOrderService;
import com.guohuai.mmp.investor.coupon.InvestorCouponOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.TradeUtil;

@Service
@Transactional
public class InvestorCashFlowService {
	
	Logger logger = LoggerFactory.getLogger(InvestorCashFlowService.class);
	@Autowired
	private InvestorCashFlowDao investorCashFlowDao;
	@Autowired
	private InvestorBankOrderService investorBankOrderService;
	
	
	public InvestorCashFlowEntity saveEntity(InvestorCashFlowEntity cashFlow) {
		return investorCashFlowDao.save(cashFlow);
	}
	
	public InvestorCashFlowEntity createCashFlow(InvestorCouponOrderEntity order) {
		InvestorCashFlowEntity cashFlow = new InvestorCashFlowEntity();
		cashFlow.setCouponOrder(order);
		cashFlow.setInvestorBaseAccount(order.getInvestorBaseAccount());
		cashFlow.setTradeAmount(order.getCouponAmount());
		if (InvestorCouponOrderEntity.TYPE_couponType_cashCoupon.equals(order.getCouponType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_cashCoupon);
		}
		return this.saveEntity(cashFlow);
	}


	public InvestorCashFlowEntity createCashFlow(InvestorBankOrderEntity bankOrder) {
		InvestorCashFlowEntity cashFlow = new InvestorCashFlowEntity();
		cashFlow.setBankOrder(bankOrder);
		cashFlow.setInvestorBaseAccount(bankOrder.getInvestorBaseAccount());
		cashFlow.setTradeAmount(bankOrder.getOrderAmount());
		if (InvestorBankOrderEntity.BANKORDER_orderType_deposit.equals(bankOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_deposit);
		}
		
		if (InvestorBankOrderEntity.BANKORDER_orderType_withdraw.equals(bankOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_withdraw);
		}
		if (InvestorBankOrderEntity.BANKORDER_feePayer_user.equals(bankOrder.getFeePayer())) {
			this.createCashFlowWithTradeType(bankOrder, InvestorCashFlowEntity.CASHFLOW_tradeType_fee);
		}
		return this.saveEntity(cashFlow);
	}



	private InvestorCashFlowEntity createCashFlowWithTradeType(InvestorBankOrderEntity bankOrder, String tradeType) {
		InvestorCashFlowEntity cashFlow = new InvestorCashFlowEntity();
		cashFlow.setBankOrder(bankOrder);
		cashFlow.setInvestorBaseAccount(bankOrder.getInvestorBaseAccount());
		cashFlow.setTradeAmount(bankOrder.getFee());
		cashFlow.setTradeType(tradeType);
		return this.saveEntity(cashFlow);
		
	}


	public InvestorCashFlowEntity createCashFlow(InvestorTradeOrderEntity tradeOrder) {
		InvestorCashFlowEntity cashFlow = new InvestorCashFlowEntity();
		cashFlow.setTradeOrder(tradeOrder);
		cashFlow.setInvestorBaseAccount(tradeOrder.getInvestorBaseAccount());
		cashFlow.setTradeAmount(tradeOrder.getOrderAmount());
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_invest);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_fastRedeem.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_fastRedeem);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_normalRedeem);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_incrementRedeem);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_specialRedeem);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_clearRedeem.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_clearRedeem);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_repayInterest.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_repayInterest);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_repayLoan.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_repayLoan);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_cash);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_refund.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_refund);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_buy.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_buy);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_profitInvest);
		}
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_noPayInvest);
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem.equals(tradeOrder.getOrderType())) {
			cashFlow.setTradeType(InvestorCashFlowEntity.CASHFLOW_tradeType_bfPlusRedeem);
		}

		return this.saveEntity(cashFlow);
	}



	public PagesRep<InvestorCashFlowQueryRep> query(Specification<InvestorCashFlowEntity> spec, Pageable pageable) {
		Page<InvestorCashFlowEntity> cas = this.investorCashFlowDao.findAll(spec, pageable);
		PagesRep<InvestorCashFlowQueryRep> pagesRep = new PagesRep<InvestorCashFlowQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (InvestorCashFlowEntity entity : cas) {
				InvestorCashFlowQueryRep queryRep = new InvestorCashFlowQueryRep();
				// 充值、提现
				if (null != entity.getBankOrder()) {
					InvestorBankOrderEntity bankOrder = entity.getBankOrder();
					queryRep.setOrderCode(bankOrder.getOrderCode());
					queryRep.setOrderType(bankOrder.getOrderType());
					queryRep.setOrderTypeDisp(this.investorBankOrderService.orderTypeEn2Ch(bankOrder.getOrderType()));
					queryRep.setTradeAmount(entity.getTradeAmount());
					queryRep.setCreateTime(bankOrder.getCreateTime());
					queryRep.setFeePayer(bankOrder.getFeePayer());
					queryRep.setFeePayerDisp(investorBankOrderService.feePayerEn2Ch(bankOrder.getFeePayer()));
					queryRep.setPayFee(bankOrder.getFee());
					queryRep.setOrderStatus(bankOrder.getOrderStatus());
					queryRep.setOrderStatusDisp(this.investorBankOrderService.orderStatusEn2Ch(bankOrder.getOrderStatus()));
				}
				// 申购、赎回
				if (null != entity.getTradeOrder()) {
					InvestorTradeOrderEntity tradeOrder = entity.getTradeOrder();
					queryRep.setOrderCode(tradeOrder.getOrderCode());
					queryRep.setOrderType(tradeOrder.getOrderType());
					queryRep.setOrderTypeDisp(TradeUtil.orderTypeEn2Ch(tradeOrder.getOrderType()));
					queryRep.setTradeAmount(entity.getTradeAmount());
					queryRep.setCreateTime(tradeOrder.getOrderTime());
					if (null != tradeOrder.getChannel()) {
						queryRep.setChannelName(tradeOrder.getChannel().getChannelName());
					}
					queryRep.setOrderStatus(tradeOrder.getOrderStatus());
					queryRep.setOrderStatusDisp(TradeUtil.orderStatusEn2Ch(tradeOrder.getOrderStatus()));
					queryRep.setIsAuto(tradeOrder.getIsAuto());//是否是自动划扣
				}
//				// 卡券：红包
//				if (null != entity.getCouponOrder()) {
//					InvestorCouponOrderEntity couponOrder = entity.getCouponOrder();
//					queryRep.setOrderCode(couponOrder.getOrderCode());
//					queryRep.setOrderType(couponOrder.getCouponType());
//					queryRep.setOrderTypeDisp(this.investorCouponOrderService.couponTypeEn2Ch(couponOrder.getCouponType()));
//					queryRep.setTradeAmount(couponOrder.getCouponAmount());
//					queryRep.setCreateTime(couponOrder.getCreateTime());
//					if (null != couponOrder.getChannel()) {
//						queryRep.setChannelName(couponOrder.getChannel().getChannelName());
//					}
//					queryRep.setOrderStatus(couponOrder.getOrderStatus());
//					queryRep.setOrderStatusDisp(this.investorCouponOrderService.orderStatusEn2Ch(couponOrder.getOrderStatus()));
//				} else {
//					queryRep.setOrderType(entity.getTradeType());
//					queryRep.setOrderTypeDisp(this.tradeTypeEn2Ch(entity.getTradeType()));
//					queryRep.setTradeAmount(entity.getTradeAmount());
//					queryRep.setCreateTime(entity.getCreateTime());
//				}
				
				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}



	public PagesRep<InvestorCashFlowQueryCRep> query4Client(Specification<InvestorCashFlowEntity> spec,
			Pageable pageable) {
		Page<InvestorCashFlowEntity> cas = this.investorCashFlowDao.findAll(spec, pageable);
		PagesRep<InvestorCashFlowQueryCRep> pagesRep = new PagesRep<InvestorCashFlowQueryCRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (InvestorCashFlowEntity entity : cas) {
				InvestorCashFlowQueryCRep queryRep = InvestorCashFlowQueryCRep.builder()
						.orderType(entity.getTradeType())
						.orderTypeDisp(tradeTypeEn2Ch(entity.getTradeType()))
						.tradeAmount(entity.getTradeAmount())
						.createTime(entity.getCreateTime())
						.build();
				//-------------申购冲销（specialRedeem）----将文案放到交易类型里---------2017.04.05-------
				if("specialRedeem".equals(queryRep.getOrderType())){
					queryRep.setOrderType(tradeTypeEn2Ch(queryRep.getOrderType()));
				}
				//-------------申购冲销（specialRedeem）----将文案放到交易类型里---------2017.04.05-------
				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	
	

	private String tradeTypeEn2Ch(String tradeType) {
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_invest.equals(tradeType)) {
			return "投资";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_expInvest.equals(tradeType)) {
			return "投资(体验金)";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_normalRedeem.equals(tradeType)
			|| InvestorCashFlowEntity.CASHFLOW_tradeType_incrementRedeem.equals(tradeType)) {
			return "赎回";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_normalExpRedeem.equals(tradeType)) {
			return "赎回(体验金)";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_fastRedeem.equals(tradeType)) {
			return "快赎";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_clearRedeem.equals(tradeType)) {
			return "赎回(清盘)";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_deposit.equals(tradeType)) {
			return "充值";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_withdraw.equals(tradeType)) {
			return "提现";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_repayLoan.equals(tradeType)) {
			return "还本";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_repayInterest.equals(tradeType)) {
			return "付息";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_fee.equals(tradeType)) {
			return "手续费";
		}
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_cash.equals(tradeType)) {
			return "还本付息";
		}
		
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_buy.equals(tradeType)) {
			return "买卖单";
		}
		
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_refund.equals(tradeType)) {
			return "退款";
		}
		
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_cashCoupon.equals(tradeType)) {
			return "红包提现";
		}
		//--------申购冲销--------2017.04.01------
		if (InvestorCashFlowEntity.CASHFLOW_tradeType_specialRedeem.equals(tradeType)) {
			return "申购冲销";
		}
		//--------申购冲销--------2017.04.01------
		
		return tradeType;
	}
	
	
}
