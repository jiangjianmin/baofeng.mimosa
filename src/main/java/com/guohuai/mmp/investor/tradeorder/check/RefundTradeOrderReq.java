package com.guohuai.mmp.investor.tradeorder.check;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class RefundTradeOrderReq {


	/**
	 * 退款金额
	 */
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	@NotNull(message = "金额不能为空")
	BigDecimal orderAmount;
	
	@NotEmpty(message = "原始订单号不能为空")
	String orderCode;
	
}
