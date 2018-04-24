package com.guohuai.bfsms;

public class BfSMSTypeEnum {

	/**
	 * 暴风短信类型
	 * @author xjj
	 *
	 */
	public enum smstypeEnum {  
        // 注册，绑卡，快速登录，忘记登录密码，认购成功，认购失败，转出，其他,38节活动,产品份额补充定时提醒,活期产品收益分配提醒,现金风暴注册,现金风暴重置密码
		regist, bindbank, login, fogetlogin, chargesucc, chargefail, withdraw, normal,womenday, sharesupplement, incomedistrinotice,xjfb_regist,xjfb_resetPW,CheckOldPhone,changeNewPhone,jobAlerm,incomedistriAuditnotice, volumeInsufficienteNotice,resetLoginPwd,resetPayPwd; 
    }  
}
