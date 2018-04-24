package com.guohuai.ams.system.config.risk.warning.collect.handle;

import java.util.List;

import com.guohuai.component.web.view.PageResp;

public class RiskWarningHandleHisListResp extends PageResp<RiskWarningHandleHisDetResp> {

	public RiskWarningHandleHisListResp() {
		super();
	}

	public RiskWarningHandleHisListResp(List<RiskWarningHandleHisDetResp> approvals) {
		this(approvals, approvals.size());
	}

	public RiskWarningHandleHisListResp(List<RiskWarningHandleHisDetResp> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}

}
