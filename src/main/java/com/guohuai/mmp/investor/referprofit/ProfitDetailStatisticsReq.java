package com.guohuai.mmp.investor.referprofit;

import lombok.Data;

@Data
public class ProfitDetailStatisticsReq {

	/**
	 * 默认页数
	 */
	private int page = 1;
	
	/**
	 * 默认行数
	 */
	private int row = 10;
	
	/**
	 * 用户姓名
	 */
	private String userName;
	
	/**
	 * 用户邀请类型（投资人、一级邀请人、二级邀请人）
	 */
	private String inviteType;
	
	/**
	 * 产品类型（活期、定期）
	 */
	private String productType;
	
	/**
	 * 产品简称
	 */
	private String productShortName;
	
	/**
	 * 创建开始时间
	 */
	private String createTimeBegin;
	
	/**
	 * 创建结束时间
	 */
	private String createTimeEnd;
}
