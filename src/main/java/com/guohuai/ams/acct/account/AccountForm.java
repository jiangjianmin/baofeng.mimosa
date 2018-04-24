package com.guohuai.ams.acct.account;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class AccountForm {

	private String oid;
	
	@NotNull(message = "名称不能为空！")	
	@Length(max = 30, message = "名称长度不能超过30（包含）！")
	private String name;

}
