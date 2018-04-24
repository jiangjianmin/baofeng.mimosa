package com.guohuai.mmp.investor.bank;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@lombok.Data
public class BankValid4EleReq implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String investorOid;
	
	private String memberOid;
		
	@NotBlank(message = "姓名不能为空！")
	private String name;
	
	@NotBlank(message = "身份证号不能为空！")
	@Length(min = 18, max = 18, message = "身份证位数格式不正确！")
	private String idCardNo;
	
	@NotBlank(message = "银行名称不能为空！")
	private String bankName;
	
	@NotBlank(message = "银行卡号不能为空！")
	@Length(min = 16, max = 19, message = "银行卡号位数格式不正确！")
	private String cardNo;
	
	@NotBlank(message = "预留手机号不能为空！")
	private String phone;
	
	private String reuqestNo;
	
	private String systemSource;
	
	@NotBlank(message = "短信验证码不能为空！")
	@Length(min = 6, max = 6, message = "短信验证码位数格式不正确！")
	private String vericode;
}
