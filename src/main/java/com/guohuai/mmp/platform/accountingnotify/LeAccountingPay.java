package com.guohuai.mmp.platform.accountingnotify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LeAccountingPay extends LeAccounting {
	private String depart_order_number;   //机构订单号 银行生成的订单号  可null
	private String order_number;    //业务订单号 支付系统生成的订单号 可null
	private String account_type;    //spv账户类型 虚拟账户（UPSPLAT）或银行结算账户（BNKCARD）1
	private String account_info;    //spv银行卡号 录入
	private String product_type;  //产品属性  定期(PERIODIC)Or活期  (CURRENT)
	private String product_code;      //产品编码
    
	private String risk_level;        //风险等级  1 低  2 中 3 高
	private String start_date;        //起息日  date
	private String product_duration;   //产品期限
	private String financing_type;    //融资类型  1:受益权转让；2：私募债；3：理财产品
	private String expect_annual_rate;   //预期年化收益率 小数
	private String bearing_base;        //计算基数，360或365  1,2
	private String balanced_account; //SPV计算的轧差金
	private String is_combine_pay; //是否合并支付
	private String account_amount; //实记金额

}
