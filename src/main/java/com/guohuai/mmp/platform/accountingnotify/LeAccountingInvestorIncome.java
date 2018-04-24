package com.guohuai.mmp.platform.accountingnotify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LeAccountingInvestorIncome extends LeAccounting {
	private String product_name;     //产品名称
	private String lx_order_number; //乐信平台业务订单号
	private String start_date;      //起息日
	private String prov_start_date; //本次计息开始日
	private String prov_end_date;    //本次计息结束日
	private String prov_customer_rvn;    //计提客户利息金额
	private String product_type = "CURRENT";     //产品属性
	private String product_code;      //产品编码
	private String basic_profit_interest;    //计提客户基础收益利息
	private String step_profit_interest;    //计提客户阶梯收益利息
	private String step_profit_rate;   //阶梯收益率
	private String bearing_base;  //计息基数
	private String book_value;   //理财产品金额
}
