package com.guohuai.mmp.investor.tradeorder;

import org.hibernate.validator.constraints.NotBlank;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class TradeOrderIsDoneReq {
	@NotBlank(message = "订单号OID不能为空！")
	String tradeOrderOid;
}
