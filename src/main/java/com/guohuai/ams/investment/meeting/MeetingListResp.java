package com.guohuai.ams.investment.meeting;

import java.util.List;

import org.springframework.data.domain.Page;

import com.guohuai.ams.investment.InvestmentMeeting;
import com.guohuai.component.web.view.PageResp;

//import com.guohuai.asset.hill.component.web.view.PageResp;

public class MeetingListResp extends PageResp<InvestmentMeeting> {

	public MeetingListResp() {
		super();
	}

	public MeetingListResp(Page<InvestmentMeeting> Approvals) {
		this(Approvals.getContent(), Approvals.getTotalElements());
	}

	public MeetingListResp(List<InvestmentMeeting> approvals) {
		this(approvals, approvals.size());
	}

	public MeetingListResp(List<InvestmentMeeting> Approvals, long total) {
		this();
		super.setTotal(total);
		super.setRows(Approvals);
	}

}
