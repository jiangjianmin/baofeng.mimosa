package com.guohuai.bfsms;

import org.hibernate.validator.constraints.NotBlank;

import com.guohuai.component.web.parameter.validation.Enumerations;

@lombok.Data
public class BfSMSReq {

	@NotBlank(message = "手机号码不能为空！")
	String phone;
	
	/** tulip发送红包成功，给用户发送短信使用，tulip没有phone，只有investorOid，需要根据investorOid从mimosa查询用户手机号 */
//	String investorOid;

	@Enumerations(values = { "regist", "bindbank", "login", "fogetlogin", "chargesucc", "chargefail", "withdraw", "normal","womenday","xjfb_regist","xjfb_resetPW","CheckOldPhone","changeNewPhone","red_packet","rate_coupon","activity","resetLoginPwd","resetPayPwd"}, message = "短信类型参数有误！")
	String smsType;
	
	/** 图形验证码 */
	String imgvc;
	
	String veriCode;
	
	String[] values;
}
