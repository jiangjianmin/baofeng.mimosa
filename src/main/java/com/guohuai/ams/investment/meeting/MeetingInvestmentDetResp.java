package com.guohuai.ams.investment.meeting;

import java.sql.Timestamp;

import com.guohuai.ams.investment.Investment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 过会标的详情
 * 
 * @author lirong
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MeetingInvestmentDetResp extends Investment {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7955097708507385431L;
	private String[] checkConditions = new String[0]; // 检查项
	private String rejectComment = ""; // 驳回理由
	private String voteStatus = "approve"; // 标的决议 通过/驳回
	private String meetingOid; // 会议oid
	private String meetingTitle;
	private Timestamp meetingTime;
	private String meetingState;
}
