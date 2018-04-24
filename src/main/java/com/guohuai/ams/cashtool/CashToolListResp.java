package com.guohuai.ams.cashtool;

import java.util.List;

import org.springframework.data.domain.Page;

import com.guohuai.component.web.view.PageResp;

//import com.guohuai.asset.hill.component.web.view.PageResp;

public class CashToolListResp extends PageResp<CashTool> {

	public CashToolListResp() {
		super();
	}

	public CashToolListResp(Page<CashTool> Approvals) {
		this(Approvals.getContent(), Approvals.getTotalElements());
	}

	public CashToolListResp(List<CashTool> approvals) {
		this(approvals, approvals.size());
	}

	public CashToolListResp(List<CashTool> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}

}
