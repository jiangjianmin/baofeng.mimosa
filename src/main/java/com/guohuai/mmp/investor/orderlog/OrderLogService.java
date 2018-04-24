package com.guohuai.mmp.investor.orderlog;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.platform.notify.NotifyService;

@Service
@Transactional
public class OrderLogService {
	Logger logger = LoggerFactory.getLogger(OrderLogService.class);
	@Autowired
	OrderLogDao orderLogDao;

	@Autowired
	NotifyService notifyService;

	public OrderLogEntity save(OrderLogEntity orderLog) {
		return this.orderLogDao.save(orderLog);
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public OrderLogEntity create(OrderLogEntity orderLog) {
		orderLog = save(orderLog);

		this.notifyService.create(orderLog.getOrderStatus(), JSONObject.toJSONString(orderLog));

		return orderLog;
	}

	/**
	 * 创建赎回结算日志
	 * 
	 * @param orderCode
	 * @param orderStatus
	 * @param orderType
	 * @param e
	 * @return
	 */
	public OrderLogEntity createRedeemCloseLog(InvestorTradeOrderEntity investOrder,
			InvestorTradeOrderEntity redeemOrder) {
		OrderLogEntity orderLog = new OrderLogEntity();
		orderLog.setTradeOrderOid(investOrder.getOrderCode());
		orderLog.setOrderStatus(investOrder.getHoldStatus());
		orderLog.setReferredOrderCode(redeemOrder.getOrderCode());
		orderLog.setReferredOrderAmount(redeemOrder.getOrderAmount());
		orderLog.setOrderType(OrderLogEntity.ORDERLOG_orderType_investClose);
		orderLog = save(orderLog);
		this.notifyService.create(investOrder.getHoldStatus(), JSONObject.toJSONString(orderLog));
		return orderLog;
	}

}
