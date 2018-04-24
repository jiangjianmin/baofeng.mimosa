package com.guohuai.mmp.platform.accountingnotify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LeAccountingPlatformFee extends LeAccounting {
	private String return_service_expense; // 非坐扣方式，应付的乐信服务费
	private String product_type;     //产品属性
	private String product_code;      //产品编码
}
