package com.guohuai.mmp.platform.accountingnotify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LeAccountingPayPlatformFee extends LeAccounting {
	private String depart_order_number;   //机构订单号 银行生成的订单号  可null
	private String order_number;    //业务订单号 支付系统生成的订单号 可null
	private String account_type;    //spv账户类型 虚拟账户（UPSPLAT）或银行结算账户（BNKCARD）1
	private String account_info;    //spv银行卡号 录入
	private String paid_service_expense; // 实付的乐信服务费
	private String product_type;     //产品属性
	private String product_code;      //产品编码
	private String is_combine_pay; //是否合并支付
	private String account_amount; //实记金额
}
