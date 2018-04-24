package com.guohuai.ams.investment.manage;

import java.util.List;

import org.springframework.data.domain.Page;

import com.guohuai.component.web.view.PageResp;

/**
 * 检查项列表返回域
 * 
 * @author lirong
 *
 */
public class InvestmentCheckListResp extends PageResp<InvestmentCheckDetResp> {

	public InvestmentCheckListResp() {
		super();
	}

	public InvestmentCheckListResp(Page<InvestmentCheckDetResp> Approvals) {
		this(Approvals.getContent(), Approvals.getTotalElements());
	}

	public InvestmentCheckListResp(List<InvestmentCheckDetResp> approvals) {
		this(approvals, approvals.size());
	}

	public InvestmentCheckListResp(List<InvestmentCheckDetResp> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}

}
