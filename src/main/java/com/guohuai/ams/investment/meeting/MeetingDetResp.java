package com.guohuai.ams.investment.meeting;

import com.guohuai.ams.investment.InvestmentMeeting;
import com.guohuai.component.web.view.BaseResp;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MeetingDetResp extends BaseResp {

	public MeetingDetResp(InvestmentMeeting meeting) {
		this.data = meeting;
	}
	private InvestmentMeeting data;
}
