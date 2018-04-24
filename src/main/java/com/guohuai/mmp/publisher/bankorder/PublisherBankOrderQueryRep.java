package com.guohuai.mmp.publisher.bankorder;

import java.math.BigDecimal;
import java.sql.Timestamp;


import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherBankOrderQueryRep {
	/**
	 * 订单号
	 */
	private String orderCode;

	/**
	 * 交易类型
	 */
	private String orderTypeDisp;
	private String orderType;

	/**
	 * 手续费支付方
	 */
	private String feePayerDisp;
	private String feePayer;

	/**
	 * 手续费
	 */
	private BigDecimal fee;

	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount;

	/**
	 * 订单状态
	 */
	private String orderStatusDisp;
	private String orderStatus;

	/**
	 * 订单创建时间
	 */
	private Timestamp createTime;
	/**
	 * 订单完成时间
	 */
	private Timestamp completeTime;

}
