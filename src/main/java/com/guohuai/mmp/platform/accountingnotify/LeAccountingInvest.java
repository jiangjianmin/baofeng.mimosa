package com.guohuai.mmp.platform.accountingnotify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LeAccountingInvest extends LeAccounting {
	private String lx_order_number; //乐信平台业务订单号 
	private String book_value;    //理财产品金额
	private String product_name; //产品名称
	private String product_type; //产品属性
	private String product_code; //产品编码
}
