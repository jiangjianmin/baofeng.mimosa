package com.guohuai.ams.system.config.risk.warning.collect.handle;

import lombok.Data;

@Data
public class RiskWarningHandleDetResp {

	private String oid;
	private String riskType;
	private String riskName;
	private String riskDet;
	private String wLevel;
	private String handleLevel;
	private String riskData;
	private String riskUnit;
	private String relative;
	private String relativeName;

}
