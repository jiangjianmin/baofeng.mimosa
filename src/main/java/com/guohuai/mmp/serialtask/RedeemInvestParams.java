package com.guohuai.mmp.serialtask;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class RedeemInvestParams {
	
	/**
	 * 赎回订单号
	 */
	private String redeemOrderCode;
	
	/**
	 * 投资订单号
	 */
	private String investOrderCode;
	
	
}
