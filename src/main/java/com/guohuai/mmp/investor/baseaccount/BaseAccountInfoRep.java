package com.guohuai.mmp.investor.baseaccount;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BaseAccountInfoRep extends BaseRep {
	
	String investorOid;
	String userOid;
	String userAcc;
	boolean userPwd;
	boolean paypwd;
	String sceneid;
	String status;
	String source;
	String channelid;
	String createTime;
	boolean islogin;
	
	String name;
	String fullName;
	String idNumb;
	String fullIdNumb;
	String bankName;
	String bankCardNum;
	String fullBankCardNum;
	String bankPhone;

}
