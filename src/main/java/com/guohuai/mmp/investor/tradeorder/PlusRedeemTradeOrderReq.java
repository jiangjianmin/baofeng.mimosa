package com.guohuai.mmp.investor.tradeorder;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlusRedeemTradeOrderReq {

	/**
	 * 所属理财产品
	 */
	@NotBlank(message = "所属理财产品OID不能为空 ")
	String productOid;

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

	/**
	 * 订单类型
	 */
	String orderType;
	/**
	 * 本金金额
	 * ps：快定宝添加字段。存放赎回订单本金金额
	 */
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	@NotNull(message = "金额不能为空")
	BigDecimal baseAmount;
	/**
	 * 订单号
	 */
	String orderCode;
}
