package com.guohuai.mmp.platform.reserved.account;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ReservedAccountReq {
	
	
	@Digits(fraction = 2, integer = 18, message = "金额输入有误，请重新输入！")
	@DecimalMin(value = "0", inclusive = false, message = "金额必须大于0！")
	@NotNull(message = "金额不能为空！")
	private BigDecimal orderAmount; // 金额
	
	private String relatedAcc;
	
}
