package com.guohuai.mmp.investor.bankorder;

import org.hibernate.validator.constraints.NotBlank;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class BankOrderIsDoneReq {
	@NotBlank(message = "订单号OID不能为空！")
	String bankOrderOid;
}
