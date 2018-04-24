package com.guohuai.ams.acct.books;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountBookResp {

	public AccountBookResp(AccountBook b) {
		this.oid = b.getOid();
		this.relative = b.getRelative();
		this.balance = b.getBalance();
		this.openingBalance = b.getOpeningBalance();

		this.accountOid = b.getAccount().getOid();
		this.accountType = b.getAccount().getType();
		this.accountCode = b.getAccount().getCode();
		this.accountName = b.getAccount().getName();
		this.accountDirection = b.getAccount().getDirection();
		this.accountState = b.getAccount().getState();
	}

	private String oid;
	private String relative;
	private BigDecimal balance;
	private BigDecimal openingBalance;

	private String accountOid;
	private String accountType;
	private String accountCode;
	private String accountName;
	private String accountDirection;
	private String accountState;

}
