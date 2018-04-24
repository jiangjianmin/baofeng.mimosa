package com.guohuai.mmp.investor.baseaccount;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
@lombok.Data
public class InvestorBaseAccountPayPwdReq {

	private String investorOid;
	
	@NotBlank(message = "支付密码不能为空！")
	@Length(min = 6, max = 6, message = "支付密码位数不正确！")
	private String payPwd;
	
}
