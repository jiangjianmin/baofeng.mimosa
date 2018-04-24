package com.guohuai.mmp.investor.baseaccount;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.guohuai.basic.component.ext.web.parameter.validation.Enumerations;

@lombok.Data
public class InvestorBaseAccountPasswordReq {
	
	private String investorOid;
	
	private String userAcc;
	
	@NotBlank(message = "密码不能为空！")
	@Length(min = 6, max = 16, message = "密码位数在6-16之间！")
	private String userPwd;
	
	@NotBlank(message = "关联应用平台不能为空！")
	@Enumerations(values = {"app", "pc" , "wx"}, message = "关联应用平台错误！")
	private String platform;
	
	private String vericode;
}
