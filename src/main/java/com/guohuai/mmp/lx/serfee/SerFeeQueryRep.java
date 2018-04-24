package com.guohuai.mmp.lx.serfee;


import java.math.BigDecimal;
import java.sql.Date;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SerFeeQueryRep {

	/**
	 * 渠道号
	 */
	private String channelOid;
	
	/**
	 * 计提费用合计
	 */
	private BigDecimal accruedSumFee;
	
	/**
	 * 某一天的费用
	 */
	private BigDecimal fee;
	
	/**
	 * 日期
	 */
	private Date tday;
	
	/**
	 * 产品id
	 */
	private String productOid;
	
	/**
	 * 渠道费率
	 */
	private BigDecimal feePercent;
	
	/**
	 * 支付平台费用合计
	 */
	private BigDecimal payPlatformSumFee;
	
	/**
	 * 平台费用手续费合计
	 */
	private BigDecimal payPlatformCouSumFee;
}
