package com.guohuai.mmp.investor.tradeorder.check;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;

import org.hibernate.validator.constraints.NotEmpty;

@lombok.Data
public class AbandonReq {
	
	@NotEmpty(message = "原始订单号不能为空")
	private String orderCode;
	
	@Digits(fraction = 2, integer = 18, message = "退款金额格式有误")
	private BigDecimal orderAmount;
}
