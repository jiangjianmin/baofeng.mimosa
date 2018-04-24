package com.guohuai.ams.acct.account;

import lombok.Data;

@Data
public class AccountResp {

	public AccountResp(Account a) {

		this.oid = a.getOid();
		this.type = a.getType();
		this.code = a.getCode();
		this.name = a.getName();
		this.direction = a.getDirection();
		this.state = a.getState();
		this.parent = a.getParent();

	}

	private String oid;
	private String type;
	private String code;
	private String name;
	private String direction;
	private String state;
	private String parent;

}
