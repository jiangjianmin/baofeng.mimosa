package com.guohuai.mmp.investor.referprofit;

import lombok.Data;

@Data
public class ProfitProvideDetailStatisticsReq {

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
	 * 用户邀请类型（待发放、已发放）
	 */
	private String provideRewardStatus;
	
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
	private String provideTimeBegin;
	
	/**
	 * 创建结束时间
	 */
	private String provideTimeEnd;
}
