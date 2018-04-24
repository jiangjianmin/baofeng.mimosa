package com.guohuai.mmp.investor.cashflow;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestorCashFlowQueryRep {

	/**
	 * 订单号
	 */
	String orderCode;

	/**
	 * 交易类型
	 */
	String orderType;
	String orderTypeDisp;
	
	/**
	 * 交易金额
	 */
	BigDecimal tradeAmount;

	/**
	 * 交易时间
	 */
	Timestamp createTime;

	/**
	 * 渠道名称
	 */
	String channelName;

	/**
	 * 手续费支付方
	 */
	String feePayer;
	String feePayerDisp;

	/**
	 * 手续费
	 */
	BigDecimal payFee;
	/**
	 * 订单状态
	 */
	String orderStatus;
	String orderStatusDisp;
	String isAuto;
}
