package com.guohuai.mmp.platform.accountingnotify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LeAccountingRedeem extends LeAccounting {
	private String lx_order_number; //乐信平台业务订单号
	private String paid_corpus;  //付本金额
	private String product_type = "CURRENT"; //产品属性
	private String product_code;      //产品编码
}
