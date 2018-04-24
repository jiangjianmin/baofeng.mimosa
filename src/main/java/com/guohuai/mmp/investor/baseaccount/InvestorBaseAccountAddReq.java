package com.guohuai.mmp.investor.baseaccount;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.guohuai.basic.component.ext.web.parameter.validation.Enumerations;

@lombok.Data
public class InvestorBaseAccountAddReq {
	
	@NotBlank(message = "手机号不能为空！")
	private String userAcc;
	
	@NotBlank(message = "短信验证码不能为空！")
	@Length(min = 6, max = 6, message = "短信验证码位数不对！")
	private String vericode;
	
	@NotBlank(message = "关联应用平台不能为空！")
	@Enumerations(values = {"app", "pc" , "wx"}, message = "关联应用平台错误！")
	private String platform;
	
	/**用户密码 */
	private String userPwd;
	
	/** 邀请人邀请码 */
	private String sceneId;
	
	/** 渠道id */
	private String channelid;
	
	/** 用户OID */
	private String investorOid;
	
	/** 会员OID */
	private String memberOid;
	
	/** 个推设备ID */
	private String clientId;
}
