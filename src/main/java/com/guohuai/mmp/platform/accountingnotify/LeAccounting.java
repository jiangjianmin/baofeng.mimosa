package com.guohuai.mmp.platform.accountingnotify;

import lombok.Data;


@Data
public class LeAccounting {
	private String data_date; //数据日期   录入数据日期
	private String batch_no;   //批次号
	private String company = "6030"; //机构编号   橄榄树SPV使用:6030    
	private String department = "10099999";//部门编号   公共部门:10099999
	private String source = "S603001";  //业务来源  橄榄树SPV使用：S603001
	private String serial;  //交易流水号
	private String event_code; //事件代码
	private String business_code;//业务类型编码
	private String business_date; //业务发生时间
	private String currency = "CNY";  //币种
	private String currency_rate = "1";  //汇率 1
	private String customer_id;   //交易关联方ID
	private String customer_account; //交易关联方名称 
}
