package com.guohuai.ams.investment.meeting;

import java.util.List;

import com.guohuai.component.web.view.PageResp;

/**
 * 过会标的详情列表
 * 
 * @author lirong
 *
 */

public class MeetingInvestmentListResp extends PageResp<MeetingInvestmentDetResp> {

	public MeetingInvestmentListResp() {
		super();
	}
	
	public MeetingInvestmentListResp(List<MeetingInvestmentDetResp> approvals) {
		this(approvals, approvals.size());
	}
	
	public MeetingInvestmentListResp(List<MeetingInvestmentDetResp> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}
}
