package com.guohuai.mmp.investor.bankorder;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.investor.cashflow.InvestorCashFlowService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;
import com.guohuai.mmp.platform.tulip.TulipService;

@Service
@Transactional
public class InvestorWithdrawBankOrderService extends InvestorBankOrderService {
	
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private InvestorCashFlowService investorCashFlowService;
	@Autowired
	private InvestorStatisticsService investorStatisticsService;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private TulipService tulipService;
	
	
	public static final int wdFreeTimes = 3;
	/**
	 *  体现
	 * @param {@link BankOrderReq}
	 * @return {@link BankOrderRep}
	 */
	@Transactional
	public BankOrderRep withdraw(BankOrderReq bankOrderReq, String orderCode) {
		BankOrderRep bankOrderRep = new BankOrderRep();
//		InvestorBankOrderEntity bankOrder = this.findByOrderCodeAndOrderStatusAndOrderType(orderCode,
//				InvestorBankOrderEntity.BANKORDER_orderStatus_submitted, InvestorBankOrderEntity.BANKORDER_orderType_withdraw);
//		
//		/** 判断<<投资人-基本账户>>.<<余额>>是否足够提现 */
//		investorBaseAccountService.balanceEnough(bankOrderReq.getMoneyVolume(), bankOrderReq.getUid());
//		
//		CustomerWithdrawRequest req = CustomerWithdrawRequestBuilder.n().build(); 
//		req.setIdentityId(bankOrderReq.getUid());
//		req.setOutTradeNo(bankOrder.getOrderCode());
//		BigDecimal amount = bankOrderReq.getMoneyVolume();
//		
//		/** 当日提现次数 */
//		boolean isFree = this.investorStatisticsService.checkTodayWithdrawCount(this.investorBaseAccountService.findByUid(bankOrderReq.getUid()), wdFreeTimes);
//		if (!isFree) {
//			if (PaymentChannelEntity.PAYMENTCHANNEL_PAYER_user.equals(bankOrder.getFeePayer())) {
//				if (bankOrder.getOrderAmount().compareTo(bankOrder.getFee()) < 0) {
//					// error.define[30046]=提现金额不足以抵扣手续费(CODE:30046)
//					throw AMPException.getException(30046);
//				}
//				req.setUserFee(bankOrder.getFee());
//				amount = bankOrder.getOrderAmount().subtract(bankOrder.getFee());
//			}
//		}
//		
//		req.setSummary("investor withdraw applycall");
//		req.setAmount(amount);
//		req.setReturnUrl(bankOrderReq.getReturnUrl());
//		
//		String orderStatus = InvestorBankOrderEntity.BANKORDER_orderStatus_toPay;
//		PaymentHtmlRep htmlRep = this.paymentServiceImpl.withDraw(req);
//		if (0 == htmlRep.getErrorCode()) {
//			bankOrderRep.setRetHtml(htmlRep.getRetHtml());
//			bankOrderRep.setBankOrderOid(bankOrder.getOid());
//		} else {
//			orderStatus = InvestorBankOrderEntity.BANKORDER_orderStatus_submitFailed;
//			bankOrderRep.setErrorCode(BaseRep.ERROR_CODE);
//			bankOrderRep.setErrorMessage(htmlRep.getErrorMessage());
//		}
//		bankOrder.setOrderStatus(orderStatus);
//		this.updateEntity(bankOrder);
		return bankOrderRep;
	}

	

//	public String withdrawCallBack(WithdrawStatusSync withdrawStatus) {
//		String status = PaymentLogEntity.PaymentLog_paymentStatus_success;
//		try {
//				InvestorBankOrderEntity bankOrder = this.findByOrderCodeAndOrderStatusAndOrderType(withdrawStatus.getOuter_trade_no(), 
//						InvestorBankOrderEntity.BANKORDER_orderStatus_toPay, InvestorBankOrderEntity.BANKORDER_orderType_withdraw);
//				if (InvestorBankOrderService.PAYMENT_success.equals(withdrawStatus.getWithdraw_status())) {
//					/** 创建<<投资人-资金变动明细>> */
//					investorCashFlowService.createCashFlow(bankOrder);
//					
//					/** 更新<<投资人-基本账户>>.<<余额>> */
//					//investorBaseAccountService.syncBalance(bankOrder.getInvestorBaseAccount());
//					/** 更新<<投资人-基本账户-统计>>.<<累计提现总额>><<累计提现次数>><<当日提现次数>> */
//					investorStatisticsService.updateStatistics4Withdraw(bankOrder);
//					/** 更新<<平台-统计>>.<<累计交易总额>><<投资人提现总额>> */
//					this.platformStatisticsService.updateStatistics4InvestorWithdraw(bankOrder.getOrderAmount());
//					
//					bankOrder.setOrderStatus(InvestorBankOrderEntity.BANKORDER_orderStatus_done);
//					
//					
//					this.tulipNewService.onCash(bankOrder);
//				} else {
//					bankOrder.setOrderStatus(InvestorBankOrderEntity.BANKORDER_orderStatus_payFailed);
//					status = PaymentLogEntity.PaymentLog_paymentStatus_failure;
//				}
//				
//				bankOrder.setCompleteTime(DateUtil.getSqlCurrentDate());
//				this.updateEntity(bankOrder);
//		
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//		return status;
//	}
	
}
