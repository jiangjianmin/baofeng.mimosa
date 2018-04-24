package com.guohuai.mmp.investor.baseaccount;

import org.hibernate.validator.constraints.NotBlank;

import com.guohuai.basic.component.ext.web.parameter.validation.Enumerations;

@lombok.Data
public class InvestorBaseAccountLoginReq {

	@NotBlank(message = "手机号不能为空！")
	private String userAcc;
	
	/** 密码 */
	private String userPwd;
	
	/** 验证码 */
	private String vericode;
	
	@NotBlank(message = "关联应用平台不能为空！")
	@Enumerations(values = {"app", "pc" , "wx"}, message = "关联应用平台错误！")
	private String platform;
	
	/** 个推设备ID */
	private String clientId;
}
