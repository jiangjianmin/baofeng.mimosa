package com.guohuai.moonBox.to;

import java.math.BigDecimal;
import java.util.Date;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class proticalQueryRes extends BaseRes {
	 private String investOid;
	 private String protocalOid;
	 private String protocalName;
	 private String productOid;
	 private String productName;
	 private BigDecimal protocalAmount;
	 private String protocalDate;
	 private String NextInvestDate;
	 private String BankCardNum;
	 private String FaultBankCardNum;
	 private String BankName;
}
