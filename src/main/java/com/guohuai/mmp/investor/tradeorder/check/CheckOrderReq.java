package com.guohuai.mmp.investor.tradeorder.check;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class CheckOrderReq {

	/**
	 * 所属理财产品
	 */
	@NotBlank(message = "所属理财产品OID不能为空 ")
	@Length(min = 32, max = 32, message = "所属理财产品OID有问题")
	String productOid;

	/**
	 * 申购金额
	 */
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	@NotNull(message = "金额不能为空")
	BigDecimal moneyVolume;


	@NotEmpty(message = "原始订单号不能为空")
	private String orderCode;
	
	@NotEmpty(message = "会员号号不能为空")
	String memberId;
}
