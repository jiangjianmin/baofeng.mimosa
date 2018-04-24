package com.guohuai.mmp.platform.errorlog;

import java.sql.Timestamp;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PlatformErrorLogQueryRep {

	
	private String oid;
	private String uid;
	private String reqUri;
	private String params;

	private Timestamp createTime;
}
