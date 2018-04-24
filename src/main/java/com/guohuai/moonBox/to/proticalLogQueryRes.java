package com.guohuai.moonBox.to;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class proticalLogQueryRes  extends BaseRes{
	private String investOid;
	 private String protocalOid;
	 private String protocalName;
	 private BigDecimal protocalAmount;
	 private String protocalDate;
	 private String NextInvestDate;
	 private String operateStatus;
	 public Timestamp createTime; 
}
