package com.guohuai.ams.system.config.risk.warning.collect.handle;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RiskWarningHandleForm {

	@NotNull(message = "oid不能为空")
	private String oid;
	@NotNull(message = "处置结果不能为空")
	private String handle;
	private String summary;
	private String report;
	private String reportSize;
	private String reportName;
	private String meeting;
	private String meetingSize;
	private String meetingName;
}
