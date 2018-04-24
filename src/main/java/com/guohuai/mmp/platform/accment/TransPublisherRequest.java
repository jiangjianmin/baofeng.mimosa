package com.guohuai.mmp.platform.accment;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TransPublisherRequest {
	
	/**
	 * 投资人账号
	 */
	private String accountNo;
	
	/**
	 * 金额
	 */
	private BigDecimal balance;
	
	/**
	 * 订单号
	 */
	private String orderNo;
	
	/**
	 * 产品OID
	 */
	private String relationProductNo;
	
}
