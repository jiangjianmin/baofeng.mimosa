package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RedeemTradeOrderReq {

	/**
	 * 所属理财产品
	 */
	@NotBlank(message = "所属理财产品OID不能为空 ")
	String productOid;

	/**
	 * 申购金额
	 */
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	@NotNull(message = "金额不能为空")
	BigDecimal orderAmount;
	
	String cid;
	
	String ckey;

	String uid;
	
	/**
	 * 省份
	 */
	String province;
	/**
	 * 城市
	 */
	String city;
}
