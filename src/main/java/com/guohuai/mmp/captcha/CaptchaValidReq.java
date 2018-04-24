package com.guohuai.mmp.captcha;

import org.hibernate.validator.constraints.NotBlank;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CaptchaValidReq {
	
	String sessionId;
	
	@NotBlank(message = "图形验证码不能为空！")
	String imgvc;
}
