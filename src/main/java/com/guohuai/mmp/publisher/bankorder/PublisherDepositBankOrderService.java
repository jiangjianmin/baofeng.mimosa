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
public class PublisherDepositBankOrderService {
	
	@Autowired
	private AccmentService accmentService;
	@Autowired
	private PublisherBankOrderService publisherBankOrderService;
	@Autowired
	private PublisherBaseAccountService publisherBaseAccountService;

	/**
	 *  充值--发行人
	 */
	@Transactional
	public BaseRep deposit(BankOrderReq bankOrderReq) {
		
		PublisherBaseAccountEntity baseAccount = publisherBaseAccountService.findByLoginAcc(bankOrderReq.getUid());
		/** 创建订单 */
		PublisherBankOrderEntity bankOrder = publisherBankOrderService.createDepostBankOrder(bankOrderReq);
		EnterAccRequest ireq = new EnterAccRequest();
		ireq.setInputAccountNo(baseAccount.getMemberId());
		ireq.setBalance(bankOrder.getOrderAmount());
		ireq.setOrderType(AccParam.OrderType.PLUSPLUS.toString());
		ireq.setRemark("spv deposit");
		ireq.setOrderNo(bankOrder.getOrderCode());
		
		
		BaseRep baseRep = this.accmentService.enterAccout(ireq);
		publisherBankOrderService.depositCallback(bankOrder.getOid(), baseRep);
		
		return baseRep;
	}
	
	public BaseRep depositDressUp(String orderOid) {
		return this.publisherBankOrderService.depositCallback(orderOid, new BaseRep());
	}
	
	
	



//	/**
//	 * 充值回调--发行人
//	 * @param {@link DepositStatusSync depositStatus}
//	 */
//	@Transactional
//	public String depositCallBack(DepositStatusSync depositStatus) {
//		String status = PaymentLogEntity.PaymentLog_paymentStatus_success;
//		try {
//			
//				PublisherBankOrderEntity bankOrder = this.publisherBankOrderService.findByOrderCodeAndOrderStatusAndOrderType(depositStatus.getOuter_trade_no(), 
//						PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_toPay, PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_deposit);
//				
//				if (PublisherBankOrderService.PAYMENT_success.equals(depositStatus.getDeposit_status())) {
//					
//					/** 创建<<发行人-资金变动明细>> */
//					publisherCashFlowService.createCashFlow(bankOrder);
//					/** 更新<<发行人-基本账户>>.<<账户余额>> */
//					this.publisherBaseAccountService.syncBalance(bankOrder.getPublisherBaseAccount());
//					/** 更新<<发行人-统计>>.<<累计充值总额>> */
//					publisherStatisticsService.updateStatistics4Deposit(bankOrder);
//					/** 更新<<平台-统计>>.<<累计交易总额>><<发行人充值总额>> */
//					this.platformStatisticsService.updateStatistics4PublisherDeposit(bankOrder.getOrderAmount());
//					
//					bankOrder.setOrderStatus(PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_paySuccess);
//				} else {
//					bankOrder.setOrderStatus(PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_payFailed);
//					status = PaymentLogEntity.PaymentLog_paymentStatus_failure;
//				}
//				bankOrder.setCompleteTime(DateUtil.getSqlCurrentDate());
//				this.publisherBankOrderService.saveEntity(bankOrder);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//		return status;
//	}

	
	
	
	
	
	
}
