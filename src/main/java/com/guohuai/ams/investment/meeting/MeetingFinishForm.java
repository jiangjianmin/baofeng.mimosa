package com.guohuai.ams.investment.meeting;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MeetingFinishForm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7461942881335219977L;

	@NotNull(message = "会议id不能为空")
	private String oid;
	@NotNull(message = "会议标的不能为空")
	private String targets;
	private String meetingRemark;
}
