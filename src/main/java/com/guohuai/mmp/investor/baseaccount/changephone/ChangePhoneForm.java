package com.guohuai.mmp.investor.baseaccount.changephone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePhoneForm {
	
	public static final String PHONE_CAN_USE = "01";
	public static final String PHONE_CAN_NOT_USE = "02";

	private String phoneNum = "";
	private String vericodes = "";
	private String payPassWord = "";
	private String RealName = "";
	private String IdCardNo = "";
	private String type = "";

}
