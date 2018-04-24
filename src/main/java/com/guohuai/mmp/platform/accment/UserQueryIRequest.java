package com.guohuai.mmp.platform.accment;


import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class UserQueryIRequest {
	
	private String userType;
	
}
