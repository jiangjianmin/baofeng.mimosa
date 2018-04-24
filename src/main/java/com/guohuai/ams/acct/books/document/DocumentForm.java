package com.guohuai.ams.acct.books.document;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DocumentForm {

	private String relative;
	private String ticket;
	private BigDecimal amount;
	private BigDecimal principal;
	private BigDecimal estimate;
	private BigDecimal revenue;
	private BigDecimal market;
	private BigDecimal saleable;
	private BigDecimal chargefee;
	private BigDecimal income;
}
