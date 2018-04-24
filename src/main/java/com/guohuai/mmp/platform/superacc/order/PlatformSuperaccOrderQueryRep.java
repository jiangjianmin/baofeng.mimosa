package com.guohuai.mmp.platform.superacc.order;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-基本账户
 * 
 * @author yuechao
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSuperaccOrderQueryRep {
	
	
	
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
	 * 订单完成时间
	 */
	private Timestamp completeTime;
	
	/**
	 * 关联账户
	 */
	private String relatedAcc;
	private String relatedAccDisp;
	
	private Timestamp updateTime;
	private Timestamp createTime;
	
}
