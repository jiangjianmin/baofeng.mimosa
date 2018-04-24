package com.guohuai.mmp.publisher.bankorder;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class BankOrderReq {
	/**
	 * 购买金额不能为空
	 */
	@Digits(integer = 18, fraction = 2, message = "金额格式错误18,2")
	@NotNull(message = "金额不能为空")
	private BigDecimal orderAmount;
	
	
	
	String uid;
}
