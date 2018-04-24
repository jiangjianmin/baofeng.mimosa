package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class RedeemInvestTradeOrderReq {

	/**
	 * 预购买定期理财产品
	 */
	@NotBlank(message = "所属理财产品OID不能为空 ")
	@Length(min = 32, max = 32, message = "所属理财产品OID有问题")
	private String investProductOid;

	/**
	 * 订单金额
	 */
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	@NotNull(message = "金额不能为空")
	private BigDecimal orderAmount;
	
	/**
	 * 预赎回活期产品
	 */
	@NotBlank(message = " 预赎回活期产品OID不能为空 ")
	@Length(min = 32, max = 32, message = " 预赎回活期产品OID有问题")
	private String redeemProductOid;

	
	private String cid;
	
	private String ckey;

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
	
	/**竞猜宝选项id */
	private String guessItemOid;
	private String orderType;
	/** 加息券属性  */
	BigDecimal ratio;
	Integer raiseDays;
	Integer cardId;
	Integer durationPeriodDays;
}
