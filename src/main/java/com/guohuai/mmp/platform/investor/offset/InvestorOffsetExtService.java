package com.guohuai.mmp.platform.investor.offset;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.cashflow.InvestorCashFlowService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;
import com.guohuai.mmp.platform.tulip.TulipService;

@Service
@Transactional
public class InvestorOffsetExtService {
	
	Logger logger = LoggerFactory.getLogger(InvestorOffsetExtService.class);
	@Autowired
	private InvestorOffsetDao investorOffsetDao;
	@Autowired
	private InvestorCashFlowService investorCashFlowService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private TulipService tulipNewService;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public int updateCloseStatus4Lock(String oid, String closeStatus, String closeMan){
		return investorOffsetDao.updateCloseStatus4Lock(oid, closeStatus, closeMan);
	}
	
	/**
	 * 处理回调中的单个订单
	 * 
	 * @param tradeStatus
	 */
//	@Transactional(value = TxType.REQUIRES_NEW)
//	public void handleCallbackOrder(BatchTradeStatus tradeStatus) {
//		InvestorTradeOrderEntity order = this.investorTradeOrderService.findByOrderCode(tradeStatus.getOuter_trade_no());
//		if (InvestorTradeOrderService.PAYMENT_trade_finished.equals(tradeStatus.getTrade_status())) {
//			/** 创建<<投资人-资金变动明细>> */
//			this.investorCashFlowService.createCashFlow(order);
//			/** 更新<<投资人-基本账户>>.<<余额>> */
//			//this.investorBaseAccountService.syncBalance(order.getInvestorBaseAccount());
//			
//			order.setInvestorCloseStatus(InvestorTradeOrderEntity.TRADEORDER_investorCloseStatus_closed);
//			order.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_done);
//			this.investorTradeOrderService.saveEntity(order);
//			
//			
//			this.investorOffsetDao.reduceToCloseRedeemAmount(order.getInvestorOffset().getOid());
//			
//			//推广平台事件处理
//			// TODO ----(退款事件)募集失败退款事件监听
//			// TODO ----(还本付息事件)到期兑付事件监听
//			// TODO ----(赎回事件)清盘赎回、快赎、普赎)完成到账 Q wl
//			this.tulipNewService.tulipEventDeal(order);
//		} else if (InvestorTradeOrderService.PAYMENT_pay_finished.equals(tradeStatus.getTrade_status())) {
//			return;
//		} else {
//			order.setInvestorCloseStatus(InvestorTradeOrderEntity.TRADEORDER_investorCloseStatus_closePayFailed);
//			this.investorTradeOrderService.saveEntity(order);
//		}
//		
//	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void clearDo(InvestorOffsetEntity offset) {
		this.investorOffsetDao.updateClearStatus(offset.getOid(), InvestorOffsetEntity.OFFSET_clearStatus_cleared);
		this.investorTradeOrderService.updateInvestorClearStatus(offset, InvestorTradeOrderEntity.TRADEORDER_investorClearStatus_cleared);
	}
	
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public BaseRep closeDo(InvestorOffsetEntity offset) {
		BaseRep baseRep = new BaseRep();
//		String icloseStatus = null;
//		String tCloseStatus = null;
//		if (offset.getRedeemAmount().compareTo(BigDecimal.ZERO) == 0) {
//			this.investorOffsetDao.updateCloseStatusDirectly(offset.getOid(), InvestorOffsetEntity.OFFSET_closeStatus_closed);
//			this.investorTradeOrderService.updateInvestorCloseStatusDirectly(offset, InvestorTradeOrderEntity.TRADEORDER_investorCloseStatus_closed);
//			return baseRep;
//		}
//		// 超级用户处理
//		List<InvestorTradeOrderEntity> platformRedeemOrders = this.investorTradeOrderService
//				.findToClosePlatformOrders(offset, this.investorBaseAccountService.getSuperInvestor().getOid());
//		if (!platformRedeemOrders.isEmpty()) {
//			BigDecimal redeemAmount = BigDecimal.ZERO;
//			for (InvestorTradeOrderEntity order : platformRedeemOrders) {
//				redeemAmount = redeemAmount.add(order.getOrderAmount());
//			}
//			this.investorBaseAccountService.updateBalancePlusPlus(redeemAmount, investorBaseAccountService.getSuperInvestor());
//			investorTradeOrderService.updatePlatformInvestorCloseStatus(offset,
//					this.investorBaseAccountService.getSuperInvestor().getOid(),
//					InvestorTradeOrderEntity.TRADEORDER_investorCloseStatus_closed);
//			investorOffsetDao.reduceToCloseRedeemAmount(offset.getOid(), platformRedeemOrders.size());
//		}
//
//		// 普通用户处理
//		String lastOid = "0";
//		while (true) {
//			List<InvestorTradeOrderEntity> redeemOrders = this.investorTradeOrderService.findToCloseOrders(offset,
//					this.investorBaseAccountService.getSuperInvestor().getOid(), lastOid);
//			this.logger.info("offsetCode={}, redeemOrders.size={}", offset.getOffsetCode(), redeemOrders.size());
//			if (redeemOrders.isEmpty()) {
//				break;
//			}
//			BigDecimal redeemAmount = BigDecimal.ZERO;
//			for (InvestorTradeOrderEntity order : redeemOrders) {
//				redeemAmount = redeemAmount.add(order.getOrderAmount());
//			}
//
//			baseRep = null;///this.batchPay(redeemAmount, redeemOrders);
//			if (0 != baseRep.getErrorCode()) { // 提交三方支付申请失败
//				icloseStatus = InvestorOffsetEntity.OFFSET_closeStatus_closeSubmitFailed;
//				tCloseStatus = InvestorTradeOrderEntity.TRADEORDER_investorCloseStatus_closeSubmitFailed;
//
//				this.investorOffsetDao.updateCloseStatus(offset.getOid(), icloseStatus);
//				this.investorTradeOrderService.updateInvestorCloseStatus(offset, tCloseStatus);
//			} else {
//				tCloseStatus = InvestorTradeOrderEntity.TRADEORDER_investorCloseStatus_closing;
//				this.investorTradeOrderService.updateInvestorCloseStatus(offset, tCloseStatus);
//			}
//			lastOid = redeemOrders.get(redeemOrders.size() - 1).getOid();
//		}

		return baseRep;
	}
	

//	/**
//	 * 批量代付处理
//	 * 
//	 * @param offset
//	 * @param orders
//	 */
//	public BaseRep batchPay(BigDecimal redeemAmount, List<InvestorTradeOrderEntity> orders) {
//		
//		BigDecimal platformBalance = this.platformBalanceService.getMiddleAccount4RedeemCollect();
//		if (redeemAmount.compareTo(platformBalance) > 0) {
//			this.logger.warn("balance({}) of platform middle account(还款专用) is not enough", platformBalance);
//			throw AMPException.getException(20014);
//		}
//		List<TransferToUserRequest> list = new ArrayList<TransferToUserRequest>();
//		for (InvestorTradeOrderEntity order : orders) {
//			TransferToUserRequest req = TransferToUserRequest.builder().build();
//			req.setAmount(order.getOrderAmount().setScale(2, RoundingMode.DOWN).toString());
//			req.setOut_trade_no(order.getOrderCode());
//			req.setPayee_identity_id(order.getInvestorBaseAccount().getUserOid());
//			req.setSummary(order.getOrderType());
//					
//			list.add(req);
//		}
//		BaseRep baseRep = this.paymentServiceImpl.transferToUsers(this.seqGenerator.next(CodeConstants.Investor_batch_pay), list);
//		return baseRep;
//	}

}
