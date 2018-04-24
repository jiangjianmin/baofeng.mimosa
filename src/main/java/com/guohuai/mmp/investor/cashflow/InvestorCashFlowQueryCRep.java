package com.guohuai.mmp.investor.cashflow;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestorCashFlowQueryCRep {
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

}
