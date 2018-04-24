package com.guohuai.ams.investment.meeting;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AddInvestmentMeetingForm implements Serializable {

	private static final long serialVersionUID = -5492380071587212837L;

	@NotNull(message = "标的不能为空")
	private String target;
	@NotNull(message = "参会人不能为空")
	private String participant;
	@NotNull(message = "会议编号不能为空")
	private String sn;
	@NotNull(message = "会议主题不能为空")
	private String title;
	@NotNull(message = "会议时间不能为空")
	private Timestamp conferenceTime;
}
