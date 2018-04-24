package com.guohuai.mmp.platform.reserved.account;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.platform.accment.AccParam;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.accment.EnterAccRequest;
import com.guohuai.mmp.platform.accment.TransferAccRequest;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountService;
import com.guohuai.mmp.platform.reserved.order.ReservedOrderEntity;
import com.guohuai.mmp.platform.reserved.order.ReservedOrderService;

@Service
@Transactional
public class ReservedAccountService {

	Logger logger = LoggerFactory.getLogger(ReservedAccountService.class);

	@Autowired
	private ReservedAccountDao reservedAccountDao;
	@Autowired
	private ReservedOrderService reservedOrderService;
	@Autowired
	private PlatformBaseAccountService platformBaseAccountService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private AccmentService accmentService;
	@Autowired
	private ReservedAccountServiceRequiresNew reservedAccountServiceRequiresNew;
	
	/**
	 * 备付金--中间户代收
	 */
	public BaseRep reserveCollect(ReservedAccountReq reservedAccountReq) {
		BaseRep rep = new BaseRep();
		
		ReservedAccountEntity reservedAccount = this.getReservedAccount();
		ReservedOrderEntity order = reservedOrderService.createReservedOrderCollect(reservedAccount,
				reservedAccountReq);
		TransferAccRequest ireq = new TransferAccRequest();
		if (ReservedOrderEntity.ORDER_relatedAcc_superAcc.equals(order.getRelatedAcc())) {
			ireq.setInputAccountNo(this.investorBaseAccountService.getSuperInvestor().getUserOid());
			ireq.setOutpuptAccountNo(reservedAccount.getReservedId());
			ireq.setBalance(order.getOrderAmount());
			ireq.setOrderNo(order.getOrderCode());
			ireq.setOrderType(AccParam.OrderType.RESERVED2SUPER.toString());
			ireq.setRemark("reserved 2 super");
		}
		
		if (ReservedOrderEntity.ORDER_relatedAcc_basicAcc.equals(order.getRelatedAcc())) {
			ireq.setInputAccountNo(this.platformBaseAccountService.getPlatfromBaseAccount().getPlatformUid());
			ireq.setOutpuptAccountNo(reservedAccount.getReservedId());
			ireq.setBalance(order.getOrderAmount());
			ireq.setOrderNo(order.getOrderCode());
			ireq.setOrderType(AccParam.OrderType.RESERVED2PLATFORM.toString());
			ireq.setRemark("reserved 2 platform");
		}
		
		if (ReservedOrderEntity.ORDER_relatedAcc_operationAcc.equals(order.getRelatedAcc())) {
//			ireq.setInputAccountNo(this.platformBaseAccountService.getPlatfromBaseAccount().getPlatformUid());
//			ireq.setOutpuptAccountNo(reservedAccount.getOperationId());
//			ireq.setBalance(order.getOrderAmount());
//			ireq.setOrderNo(order.getOrderCode());
//			ireq.setOrderType(AccParam.OrderType.RESERVED2.toString());
//			ireq.setRemark("reserved 2 operation");
		}
		
		BaseRep irep = this.accmentService.transferAccount(ireq);
		
		this.reservedAccountServiceRequiresNew.collectCallBack(order.getOid(), irep);
		return irep;
	}
	
	public BaseRep trade(ReservedAccountTradeReq tReq) {
		BaseRep rep = new BaseRep();
		ReservedAccountEntity reservedAccount = this.getReservedAccount();
		ReservedOrderEntity order = reservedOrderService.createReservedTradeOrder(reservedAccount,
				tReq);
		
		EnterAccRequest ireq = new EnterAccRequest();
		if (ReservedOrderEntity.ORDER_orderType_deposit.equals(order.getOrderType())) {
			ireq.setInputAccountNo(this.getReservedAccount().getReservedId());
			ireq.setBalance(order.getOrderAmount());
			ireq.setOrderType(AccParam.OrderType.PLUSPLUS.toString());
			ireq.setRemark("reservedAccount deposit");
			ireq.setOrderNo(order.getOrderCode());
		}
		
		if (ReservedOrderEntity.ORDER_orderType_withdraw.equals(order.getOrderType())) {
			ireq.setInputAccountNo(this.getReservedAccount().getReservedId());
			ireq.setBalance(order.getOrderAmount());
			ireq.setOrderType(AccParam.OrderType.MINUSMINUS.toString());
			ireq.setRemark("reservedAccount withdraw");
			ireq.setOrderNo(order.getOrderCode());
		}
		
		BaseRep irep = this.accmentService.enterAccout(ireq);
		reservedAccountServiceRequiresNew.tradeCallBack(order.getOid(), irep);
		return irep;
	}

	
	public BaseRep tradeCallBack(String orderOid) {
		return this.reservedAccountServiceRequiresNew.tradeCallBack(orderOid, new BaseRep());
	}
	
	public BaseRep collectCallBack(String orderOid) {
		return this.reservedAccountServiceRequiresNew.collectCallBack(orderOid, new BaseRep());
	}
	
	

	
	/**
	 * 备付金--中间户代付
	 * @param reservedAccountReq
	 * @param platformOid
	 * @return
	 */
	public BaseRep reservePay(ReservedAccountReq reservedAccountReq) {
		
		ReservedAccountRep rep = new ReservedAccountRep();
		
		ReservedAccountEntity reservedAccount = getReservedAccount();
		ReservedOrderEntity order = reservedOrderService.createReservedOrderPay(
				reservedAccount, reservedAccountReq);
		
		TransferAccRequest ireq = new TransferAccRequest();
		if (ReservedOrderEntity.ORDER_relatedAcc_superAcc.equals(order.getRelatedAcc())) {
			ireq.setInputAccountNo(reservedAccount.getReservedId());
			ireq.setOutpuptAccountNo(this.investorBaseAccountService.getSuperInvestor().getUserOid());
			ireq.setBalance(order.getOrderAmount());
			ireq.setOrderNo(order.getOrderCode());
			ireq.setOrderType(AccParam.OrderType.SUPER2RESERVED.toString());
			ireq.setRemark("super 2 reserved");
		}
		
		if (ReservedOrderEntity.ORDER_relatedAcc_basicAcc.equals(order.getRelatedAcc())) {
			ireq.setInputAccountNo(reservedAccount.getReservedId());
			ireq.setOutpuptAccountNo(this.platformBaseAccountService.getPlatfromBaseAccount().getPlatformUid());
			ireq.setBalance(order.getOrderAmount());
			ireq.setOrderNo(order.getOrderCode());
			ireq.setOrderType(AccParam.OrderType.PLATFORM2RESERVED.toString());
			ireq.setRemark("platform 2 reserved");
		}
		
		
		BaseRep irep = this.accmentService.transferAccount(ireq);
		
		this.reservedAccountServiceRequiresNew.payCallBack(order.getOid(), irep);
		return rep;
	}
	
	
	
//	public String collectCallBack(TradeStatusSync tradeStatus) {
//		String status = PaymentLogEntity.PaymentLog_paymentStatus_success;
//		String orderStatus = ReservedOrderEntity.ORDER_orderStatus_paySuccess;
//		try {
//			ReservedOrderEntity order = this.reservedOrderService.findByOrderCode(tradeStatus.getOuter_trade_no());
//			if (ReservedAccountService.PAYMENT_trade_finished.equals(tradeStatus.getTrade_status())) {
//				
//				
//				this.reservedAccountDao.syncBalance(
//						this.platformBalanceService.getReserveBalance(order.getReservedAccount().getPayUid()), order.getReservedAccount().getOid());
//				
//				/** 创建<<平台-备付金变动明细>> */
//				this.reservedAccountCashFlowService.createCashFlow(order);
//				
//				
//				if (ReservedOrderEntity.ORDER_relatedAcc_basicAcc.equals(order.getRelatedAcc())) {
//					this.reservedAccountDao.update4BasicCollect(order.getOrderAmount(), order.getReservedAccount().getOid());
//					//platformBaseAccountService.syncBalance(order.getOrderAmount());
//				}
//				
//				if (ReservedOrderEntity.ORDER_relatedAcc_superAcc.equals(order.getRelatedAcc())) {
//					this.reservedAccountDao.update4SuperCollect(order.getOrderAmount(), order.getReservedAccount().getOid());
//					//investorBaseAccountService.increaseSuperBalance(order.getOrderAmount());
//				}
//
//			} else if (ReservedAccountService.PAYMENT_pay_finished.equals(tradeStatus.getTrade_status())) {
//				// doing nothing
//			} else {
//				orderStatus = ReservedOrderEntity.ORDER_orderStatus_payFailed;
//				status = PaymentLogEntity.PaymentLog_paymentStatus_failure;
//			}
//			order.setOrderStatus(orderStatus);
//			this.reservedOrderService.saveEntity(order);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
		
//		return status;
//	}

//	public String payCallBack(TradeStatusSync tradeStatus) {
//		String status = PaymentLogEntity.PaymentLog_paymentStatus_success;
//		String orderStatus = ReservedOrderEntity.ORDER_orderStatus_paySuccess;
//		try {
//			ReservedOrderEntity order = this.reservedOrderService.findByOrderCode(tradeStatus.getOuter_trade_no());
//			if (ReservedAccountService.PAYMENT_trade_finished.equals(tradeStatus.getTrade_status())) {
//				
//				this.reservedAccountDao.syncBalance(platformBalanceService.getReserveBalance(
//						order.getReservedAccount().getPayUid()), order.getReservedAccount().getOid());
//				
//				/** 创建<<平台-备付金变动明细>> */
//				this.reservedAccountCashFlowService.createCashFlow(order);
//				
//				if (ReservedOrderEntity.ORDER_relatedAcc_basicAcc.equals(order.getRelatedAcc())) {
//					this.reservedAccountDao.update4BasicPay(order.getOrderAmount(), order.getReservedAccount().getOid());
//					this.platformBaseAccountService.syncBalance(order.getOrderAmount().negate());
//				}
//
//				
//				if (ReservedOrderEntity.ORDER_relatedAcc_superAcc.equals(order.getRelatedAcc())) {
//					this.reservedAccountDao.update4SuperPay(order.getOrderAmount(), order.getReservedAccount().getOid());
//					investorBaseAccountService.decreaseSuperBalance(order.getOrderAmount());
//				}
//
//			} else if (ReservedAccountService.PAYMENT_pay_finished.equals(tradeStatus.getTrade_status())) {
//				// doing nothing
//			} else {
//				orderStatus = ReservedOrderEntity.ORDER_orderStatus_payFailed;
//				status = PaymentLogEntity.PaymentLog_paymentStatus_failure;
//			}
//			order.setOrderStatus(orderStatus);
//			this.reservedOrderService.saveEntity(order);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//		
//		return status;
//	}

	public BaseRep detail() {
		ReservedAccountDetailRep rep = new ReservedAccountDetailRep();
		ReservedAccountEntity account = this.getReservedAccount();
		rep.setPayUid(account.getReservedId()); //三方支付账号
		rep.setBalance(account.getBalance()); //余额
		rep.setTotalDepositAmount(account.getTotalDepositAmount()); //累计充值总额
		rep.setTotalWithdrawAmount(account.getTotalWithdrawAmount()); //累计提现总额
		rep.setSuperAccBorrowAmount(account.getSuperAccBorrowAmount()); //超级户借款金额
		rep.setBasicAccBorrowAmount(account.getBasicAccBorrowAmount()); //基本户借款金额
		
		rep.setLastBorrowTime(account.getLastBorrowTime()); //最近借款时间
		rep.setLastReturnTime(account.getLastReturnTime()); //最近还款时间
		rep.setUpdateTime(account.getUpdateTime());
		rep.setCreateTime(account.getCreateTime());
		return rep;
	}

	
	
	public ReservedAccountEntity getReservedAccount() {
		ReservedAccountEntity account = this.reservedAccountDao.getReservedAccount();
		if (null == account) {
			throw new AMPException("备付金账户不存在");
		}
		return account;
	}

	public ReservedAccountEntity saveEntity(ReservedAccountEntity entity) {
		return this.reservedAccountDao.save(entity);
	}


	

	

}
