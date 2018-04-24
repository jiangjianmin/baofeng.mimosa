package com.guohuai.mmp.platform.reserved.account.cashflow;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.mmp.platform.reserved.order.ReservedOrderEntity;

@Service
@Transactional
public class ReservedAccountCashFlowService {

	Logger logger = LoggerFactory.getLogger(ReservedAccountCashFlowService.class);

	@Autowired
	ReservedAccountCashFlowDao reservedAccountCashFlowDao;

	public ReservedAccountCashFlowEntity createCashFlow(ReservedOrderEntity order) {
		ReservedAccountCashFlowEntity cashFlow = new ReservedAccountCashFlowEntity();
		cashFlow.setReservedAccount(order.getReservedAccount());
		cashFlow.setReservedOrderEntity(order);
		cashFlow.setTradeAmount(order.getOrderAmount());
		if (ReservedOrderEntity.ORDER_orderType_borrow.equals(order.getOrderType())) {
			cashFlow.setTradeType(ReservedAccountCashFlowEntity.CASHFLOW_tradeType_borrow);
		}
		if (ReservedOrderEntity.ORDER_orderType_return.equals(order.getOrderType())) {
			cashFlow.setTradeType(ReservedAccountCashFlowEntity.CASHFLOW_tradeType_return);
		}
		return this.saveEntity(cashFlow);
	}

	public ReservedAccountCashFlowEntity saveEntity(ReservedAccountCashFlowEntity cashFlow) {
		return this.reservedAccountCashFlowDao.save(cashFlow);
	}


}
