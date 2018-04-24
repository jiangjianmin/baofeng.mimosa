package com.guohuai.ams.system.config.risk.warning.collect.handle;

import java.util.List;

import com.guohuai.component.web.view.PageResp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RiskWarningHandleListResp extends PageResp<RiskWarningHandleDetResp> {

	public RiskWarningHandleListResp() {
		super();
	}

	public RiskWarningHandleListResp(List<RiskWarningHandleDetResp> approvals) {
		this(approvals, approvals.size());
	}

	public RiskWarningHandleListResp(List<RiskWarningHandleDetResp> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}
}
