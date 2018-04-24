package com.guohuai.mmp.publisher.bankorder;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.platform.accment.AccParam;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.accment.EnterAccRequest;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;

@Service
@Transactional
public class PublisherWithdrawBankOrderService {
	
	@Autowired
	private PublisherBaseAccountService publisherBaseAccountService;
	@Autowired
	private PublisherBankOrderService publisherBankOrderService;
	@Autowired
	private AccmentService accmentService;
	
	/**
	 *  体现
	 */
	@Transactional
	public BaseRep withdraw(BankOrderReq bankOrderReq) {
//		BankOrderRep bankOrderRep = new BankOrderRep();
		
		/** 判断<<投资人-基本账户>>.<<余额>>是否足够提现 */
		PublisherBaseAccountEntity baseAccount = publisherBaseAccountService.findByLoginAcc(bankOrderReq.getUid());
		publisherBaseAccountService.balanceEnough(bankOrderReq.getOrderAmount(), baseAccount.getOid());
		
		PublisherBankOrderEntity bankOrder = publisherBankOrderService.createWithdrawBankOrder(bankOrderReq);
		
		EnterAccRequest ireq = new EnterAccRequest();
		ireq.setInputAccountNo(baseAccount.getMemberId());
		ireq.setBalance(bankOrder.getOrderAmount());
		ireq.setOrderType(AccParam.OrderType.MINUSMINUS.toString());
		ireq.setRemark("spv withdraw");
		ireq.setOrderNo(bankOrder.getOrderCode());
		
		
		BaseRep baseRep = this.accmentService.enterAccout(ireq);
		publisherBankOrderService.withdrawCallback(bankOrder.getOrderCode(), baseRep);

		return new BaseRep();
	}
	
	public BaseRep withdrawDressUp(String orderOid) {
		return this.publisherBankOrderService.withdrawCallback(orderOid, new BaseRep());
	}
	

	



//	public String withdrawCallBack(WithdrawStatusSync withdrawStatus) {
//		String status = PaymentLogEntity.PaymentLog_paymentStatus_success;
//		try {
//				PublisherBankOrderEntity bankOrder = this.findByOrderCodeAndOrderStatusAndOrderType(withdrawStatus.getOuter_trade_no(), 
//						PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_toPay, PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_withdraw);
//				
//				if (PublisherBankOrderService.PAYMENT_success.equals(withdrawStatus.getWithdraw_status())) {
//					/** 创建<<发行人-资金变动明细>> */
//					this.publisherCashFlowService.createCashFlow(bankOrder);
//					/** 更新<<发行人-基本账户>>.<<余额>> */
//					this.publisherBaseAccountService.syncBalance(bankOrder.getPublisherBaseAccount());
//					/** 更新<<发行人-统计>>.<<累计提现总额>> */
//					this.publisherStatisticsService.updateStatistics4Withdraw(bankOrder);
//					/** 更新<<平台-统计>>.<<累计交易总额>><<发行人提现总额>> */
//					this.platformStatisticsService.updateStatistics4PublisherWithdraw(bankOrder.getOrderAmount());
//					bankOrder.setOrderStatus(PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_paySuccess);
//				} else {
//					bankOrder.setOrderStatus(PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_payFailed);
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
