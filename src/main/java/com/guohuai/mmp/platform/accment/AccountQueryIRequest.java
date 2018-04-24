package com.guohuai.mmp.platform.accment;


import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class AccountQueryIRequest {
	
	private String userType;
	private String accountType;
	private String userOid;
	
}
