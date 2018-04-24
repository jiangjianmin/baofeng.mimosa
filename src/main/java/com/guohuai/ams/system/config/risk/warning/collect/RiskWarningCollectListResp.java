package com.guohuai.ams.system.config.risk.warning.collect;

import java.util.List;

import com.guohuai.component.web.view.PageResp;

public class RiskWarningCollectListResp extends PageResp<RiskWarningCollectListInfoResp> {

	public RiskWarningCollectListResp() {
		super();
	}

	public RiskWarningCollectListResp(List<RiskWarningCollectListInfoResp> approvals) {
		this(approvals, null == approvals ? 0 : approvals.size());
	}
	

	public RiskWarningCollectListResp(List<RiskWarningCollectListInfoResp> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}
}
