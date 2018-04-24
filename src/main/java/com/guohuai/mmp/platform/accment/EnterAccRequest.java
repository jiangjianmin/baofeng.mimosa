package com.guohuai.mmp.platform.accment;

import java.math.BigDecimal;


import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class EnterAccRequest {
	
	
	/**
	 * 入账账户ID
	 */
	private String inputAccountNo;
	
	/**
	 * 订单金额
	 */
	private BigDecimal balance;
	
	/**
	 * 单据类型
	 */
	private String orderType;
	
	/**
	 * 交易用途
	 */
	private String remark;
	
	/**
	 * 定单号
	 */
	private String orderNo;
	
	
	
}
