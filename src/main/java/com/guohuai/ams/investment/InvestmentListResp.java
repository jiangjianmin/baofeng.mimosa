package com.guohuai.ams.investment;

import java.util.List;

import org.springframework.data.domain.Page;

import com.guohuai.component.web.view.PageResp;

//import com.guohuai.asset.hill.component.web.view.PageResp;

public class InvestmentListResp extends PageResp<Investment> {

	public InvestmentListResp() {
		super();
	}

	public InvestmentListResp(Page<Investment> Approvals) {
		this(Approvals.getContent(), Approvals.getTotalElements());
	}

	public InvestmentListResp(List<Investment> approvals) {
		this(approvals, null == approvals ? 0 : approvals.size());
	}

	public InvestmentListResp(List<Investment> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}

}
