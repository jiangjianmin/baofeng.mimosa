package com.guohuai.mmp.investor.fund;

import java.math.BigDecimal;
@lombok.Data
public class FundTradingVo {
	private BigDecimal applicationamount;
	private BigDecimal applicationvol;
	private String applystname;
	private BigDecimal confirmedamount;
	private BigDecimal confirmedvol;
	private String appsheetserialno;
	private String businessname;
	private String fundcode;
	private String fundname;
	private String transactiondate;
	
}
