package com.guohuai.ams.system.config.risk.warning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskWarningResp {
	String oid;
	private String indicateOid;

	String title;

	public RiskWarningResp(RiskWarning warning) {
		this.oid = warning.getOid();
		this.indicateOid = warning.getIndicate().getOid();
		this.title = warning.getTitle();
	}
}
