package com.guohuai.mmp.investor.baseaccount;

import java.sql.Date;

import com.guohuai.mmp.sys.SysConstant;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class InvestorBaseAccountRedisInfo {

	/** 输入密码错误次数 */
	private int pwdErrorTimes = SysConstant.INTEGER_defaultValue;
	
	/** 开始锁定时间 */
	private Date lockTime;
	
	/** 个推设备ID */
	private String clientId;
}
