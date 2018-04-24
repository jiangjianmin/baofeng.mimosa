package com.guohuai.mmp.platform.reserved.order;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ReservedOrderQueryRep {
	
	
	
	/**
	 * 订单号
	 */
	private String orderCode;
	
	/**
	 * 交易类型
	 */
	private String orderType;
	private String orderTypeDisp;
	
	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount;
	
	/**
	 * 订单状态
	 */
	private String orderStatus;
	private String orderStatusDisp;
	
	/**
	 * 关联账户
	 */
	private String relatedAcc;
	private String relatedAccDisp;
	
	/**
	 * 订单完成时间
	 */
	private Timestamp completeTime;
	
	/**
	 * 订单创建时间
	 */
	private Timestamp createTime;
}
