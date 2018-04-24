package com.guohuai.mmp.platform.accountingnotify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LeAccountingPayCouFee extends LeAccounting {
	private String depart_order_number;   //机构订单号 银行生成的订单号  可null
	private String order_number;    //业务订单号 支付系统生成的订单号 可null
	private String account_type;    //spv账户类型 虚拟账户（UPSPLAT）或银行结算账户（BNKCARD）1
	private String account_info;    //spv银行卡号 录入
    private String paid_pay_expense;  //轧差支付或者实付乐信费的手续费金额
}
