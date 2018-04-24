package com.guohuai.ams.acct.books;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountBookBalance {

	private String lcode = "";
	private String lname = "";
	private String lsn = "";
	private BigDecimal lbalance = BigDecimal.ZERO;

	private String rcode = "";
	private String rname = "";
	private String rsn = "";
	private BigDecimal rbalance = BigDecimal.ZERO;

}
