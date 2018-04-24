package com.guohuai.ams.system.config.risk.warning.collect.handle;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class RiskWarningHandleHisDetResp {

	private String oid;
	private String riskType;
	private String riskName;
	private String riskDet;
	private String relative;
	private String relativeName;
	private String handle;
	private Timestamp createTime;
	private String report;
	private String meeting;
	private String summary;
	private String meetingName;
	private String reportName;
	

}
