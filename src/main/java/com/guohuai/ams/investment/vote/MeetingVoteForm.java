package com.guohuai.ams.investment.vote;

import lombok.Data;

/**
 * 会议表决表单
 * 
 * @author Administrator
 *
 */
@Data
public class MeetingVoteForm {

	private String meetingOid; // 会议oid
	private String targetOid; // 标的oid
	private String voteState; // 表决状态
	private String suggest; // 表决意见
	private String file; // 附件
	private String fileName;
	private String fileSize;
}
