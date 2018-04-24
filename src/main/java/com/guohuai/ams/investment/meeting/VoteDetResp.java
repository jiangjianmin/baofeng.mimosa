package com.guohuai.ams.investment.meeting;

import java.sql.Timestamp;

import lombok.Data;

/**
 * 标的过会投票结果返回类
 * 
 * @author Administrator
 *
 */
@Data
public class VoteDetResp {

	/**
	 * 角色
	 */
	private String roll;
	/**
	 * 投票状态
	 */
	private String voteStatus;
	/**
	 * 投票人
	 */
	private String name;
	/**
	 * 投票时间
	 */
	private Timestamp time;
	/*
	 * 附件
	 */
	private String file;
	/**
	 * 附件名称
	 */
	private String fileName;
}
