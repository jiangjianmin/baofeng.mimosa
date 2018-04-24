package com.guohuai.mmp.investor.bankorder;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;

import org.hibernate.validator.constraints.Length;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class BankOrderReq {
	/**
	 * 购买金额不能为空
	 */
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	BigDecimal moneyVolume;

	/**
	 * 返回URL
	 */
	@Length(max = 500, message = "回调地址长度不能大于500")
	String returnUrl;

	String ip;
	
	String uid;
}
