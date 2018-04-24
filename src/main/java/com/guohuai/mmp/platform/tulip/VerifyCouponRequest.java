package com.guohuai.mmp.platform.tulip;

import java.math.BigDecimal;

import lombok.Data;


@Data
public class VerifyCouponRequest {
	
	
	/** 卡券编号 */
	private String couponId;

	/** 卡券类型 */
	private String couponType;

	/** 卡券实际抵扣金额 */
	private BigDecimal couponDeductibleAmount;

	/** 卡券金额 */
	private BigDecimal couponAmount;

	/** 投资者实付金额 */
	private BigDecimal payAmouont;
	
	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount;
	
	/**
	 * 产品
	 */
	private String productOid;
	private String orderType;
	private BigDecimal ratio;
	private Integer raiseDays;
	private Integer durationPeriodDays;
}
