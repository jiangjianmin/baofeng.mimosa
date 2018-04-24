package com.guohuai.mmp.investor.bankorder;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.investor.cashflow.InvestorCashFlowService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;

@Service
@Transactional
public class InvestorDepositBankOrderService extends InvestorBankOrderService {
	
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private InvestorCashFlowService investorCashFlowService;
	@Autowired
	private InvestorStatisticsService investorStatisticsService;
	@Autowired
	private InvestorBankOrderDao investorBankOrderDao;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;

	/**
	 *  充值--投资人
	 * @param {@link BankOrderReq bankOrderReq}
	 * @return {@link BankOrderRep bankOrderRep}
	 */
	@Transactional
	public BankOrderRep deposit(BankOrderReq bankOrderReq, String orderCode) {
		BankOrderRep bankOrderRep = new BankOrderRep();
//		/** 创建订单 */
//		InvestorBankOrderEntity bankOrder = this.findByOrderCodeAndOrderStatusAndOrderType(orderCode,
//				InvestorBankOrderEntity.BANKORDER_orderStatus_submitted, InvestorBankOrderEntity.BANKORDER_orderType_deposit);
//		CustomerPayRequest customerPayRequest = CustomerPayRequestBuilder.n()
//				.outTradeNo(bankOrder.getOrderCode())
//				.amount(bankOrderReq.getMoneyVolume())
//				.identityId(bankOrderReq.getUid())
//				.payerIp(bankOrderReq.getIp())
//				.summary("investor deposit applycall")
//				.returnUrl(bankOrderReq.getReturnUrl())
//				.build();
//		;
		/** 调用三方支付充值接口 */
//		PaymentHtmlRep htmlRep = this.paymentServiceImpl.deposit(customerPayRequest);
//		String orderStatus = InvestorBankOrderEntity.BANKORDER_orderStatus_toPay;
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
	
	
	
	/**
	 * 充值回调--投资人
	 * @param {@link DepositStatusSync depositStatus}
	 */
//	@Transactional
//	public String depositCallBack(DepositStatusSync depositStatus) {
//		String status = PaymentLogEntity.PaymentLog_paymentStatus_success;
//		try {
//			
//				InvestorBankOrderEntity bankOrder = this.investorBankOrderDao.findByOrderCodeAndOrderStatusAndOrderType(depositStatus.getOuter_trade_no(), 
//						InvestorBankOrderEntity.BANKORDER_orderStatus_toPay, InvestorBankOrderEntity.BANKORDER_orderType_deposit);
//				
//				if (InvestorBankOrderService.PAYMENT_success.equals(depositStatus.getDeposit_status())) {
//					
//					/** 创建<<投资人-资金变动明细>> */
//					investorCashFlowService.createCashFlow(bankOrder);
//					/** 更新<<投资人-基本账户>>.<<余额>> */
//				//	this.investorBaseAccountService.syncBalance(bankOrder.getInvestorBaseAccount());
//					/** 更新<<投资人-基本账户-统计>>.<<累计充值总额>><<累计充值次数>><<当日充值次数>><<当日充值总额>> */
//					investorStatisticsService.updateStatistics4Deposit(bankOrder);
//					/** 更新<<平台-统计>>.<<累计交易总额>><<投资人充值总额>> */
//					this.platformStatisticsService.updateStatistics4InvestorDeposit(bankOrder.getOrderAmount());
//					
//					bankOrder.setOrderStatus(InvestorBankOrderEntity.BANKORDER_orderStatus_done);
//				} else {
//					bankOrder.setOrderStatus(InvestorBankOrderEntity.BANKORDER_orderStatus_payFailed);
//					status = PaymentLogEntity.PaymentLog_paymentStatus_failure;
//				}
//				bankOrder.setCompleteTime(DateUtil.getSqlCurrentDate());
//				this.updateEntity(bankOrder);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//		return status;
//	}

	
	
	
	
	
	
}
