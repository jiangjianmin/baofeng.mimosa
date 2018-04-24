package com.guohuai.mmp.platform.reserved.account;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountService;
import com.guohuai.mmp.platform.reserved.account.cashflow.ReservedAccountCashFlowService;
import com.guohuai.mmp.platform.reserved.order.ReservedOrderEntity;
import com.guohuai.mmp.platform.reserved.order.ReservedOrderService;

@Service
@Transactional
public class ReservedAccountServiceRequiresNew {

	Logger logger = LoggerFactory.getLogger(ReservedAccountServiceRequiresNew.class);

	@Autowired
	private ReservedAccountDao reservedAccountDao;
	@Autowired
	private ReservedOrderService reservedOrderService;
	@Autowired
	private ReservedAccountCashFlowService reservedAccountCashFlowService;
	@Autowired
	private PlatformBaseAccountService platformBaseAccountService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public BaseRep payCallBack(String orderOid, BaseRep irep) {

		String orderStatus = ReservedOrderEntity.ORDER_orderStatus_paySuccess;

		ReservedOrderEntity order = this.reservedOrderService.findByOid(orderOid);
		if (0 == irep.getErrorCode()) {

			this.reservedAccountDao.updateBalancePlusPlus(order.getOrderAmount(), order.getReservedAccount().getOid());

			/** 创建<<平台-备付金变动明细>> */
			this.reservedAccountCashFlowService.createCashFlow(order);

			if (ReservedOrderEntity.ORDER_relatedAcc_basicAcc.equals(order.getRelatedAcc())) {
				this.reservedAccountDao.update4BasicPay(order.getOrderAmount(), order.getReservedAccount().getOid());
				this.platformBaseAccountService.updateBalanceMinusMinus(order.getOrderAmount().negate());
			}

			if (ReservedOrderEntity.ORDER_relatedAcc_superAcc.equals(order.getRelatedAcc())) {
				this.reservedAccountDao.update4SuperPay(order.getOrderAmount(), order.getReservedAccount().getOid());
				investorBaseAccountService.updateBalanceMinusMinus(order.getOrderAmount(),
						investorBaseAccountService.getSuperInvestor());
			}

		} else {
			orderStatus = ReservedOrderEntity.ORDER_orderStatus_payFailed;
		}
		order.setOrderStatus(orderStatus);
		this.reservedOrderService.saveEntity(order);
		return new BaseRep();
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public BaseRep collectCallBack(String orderOid, BaseRep irep) {

		String orderStatus = ReservedOrderEntity.ORDER_orderStatus_paySuccess;

		ReservedOrderEntity order = this.reservedOrderService.findByOid(orderOid);
		if (0 == irep.getErrorCode()) {

			this.reservedAccountDao.updateBalancePlusPlus(order.getOrderAmount(), order.getReservedAccount().getOid());

			/** 创建<<平台-备付金变动明细>> */
			this.reservedAccountCashFlowService.createCashFlow(order);

			if (ReservedOrderEntity.ORDER_relatedAcc_basicAcc.equals(order.getRelatedAcc())) {
				this.reservedAccountDao.update4BasicCollect(order.getOrderAmount(),
						order.getReservedAccount().getOid());
				platformBaseAccountService.updateBalancePlusPlus(order.getOrderAmount());
			}

			if (ReservedOrderEntity.ORDER_relatedAcc_superAcc.equals(order.getRelatedAcc())) {
				this.reservedAccountDao.update4SuperCollect(order.getOrderAmount(),
						order.getReservedAccount().getOid());
				investorBaseAccountService.updateBalancePlusPlus(order.getOrderAmount(),
						this.investorBaseAccountService.getSuperInvestor());
			}
		} else {
			orderStatus = ReservedOrderEntity.ORDER_orderStatus_payFailed;
		}
		order.setOrderStatus(orderStatus);
		this.reservedOrderService.saveEntity(order);

		return new BaseRep();
	}

	public BaseRep tradeCallBack(String orderOid, BaseRep irep) {
		String orderStatus = ReservedOrderEntity.ORDER_orderStatus_paySuccess;

		ReservedOrderEntity order = this.reservedOrderService.findByOid(orderOid);
		if (0 == irep.getErrorCode()) {

			/** 创建<<平台-备付金变动明细>> */
			this.reservedAccountCashFlowService.createCashFlow(order);

			if (ReservedOrderEntity.ORDER_orderType_deposit.equals(order.getOrderType())) {
				this.reservedAccountDao.increaseTotalDepositAmount(order.getOrderAmount(),
						order.getReservedAccount().getOid());
				this.reservedAccountDao.updateBalancePlusPlus(order.getOrderAmount(), order.getReservedAccount().getOid());
			}

			if (ReservedOrderEntity.ORDER_orderType_withdraw.equals(order.getOrderType())) {
				this.reservedAccountDao.increaseTotalWithdrawAmount(order.getOrderAmount(),
						order.getReservedAccount().getOid());
				this.reservedAccountDao.updateBalanceMinusMinus(order.getOrderAmount(), order.getReservedAccount().getOid());
			}
		} else {
			orderStatus = ReservedOrderEntity.ORDER_orderStatus_payFailed;
		}
		order.setOrderStatus(orderStatus);
		this.reservedOrderService.saveEntity(order);

		return new BaseRep();
	}

}
