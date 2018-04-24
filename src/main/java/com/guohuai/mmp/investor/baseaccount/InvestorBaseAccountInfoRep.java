package com.guohuai.mmp.investor.baseaccount;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestorBaseAccountInfoRep extends BaseRep {

	private String phoneNum;
	
	private String realName;
	
	private String status;
	
	private BigDecimal balance;	

	private Timestamp createTime;
	//首次投资时间
	private Timestamp firstInvestTime; 
	
}
