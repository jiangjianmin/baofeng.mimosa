package com.guohuai.mmp.platform.statistics;

import lombok.Data;

@Data
public class UserBehaviorStatisticsReq {
	
	/**
	 * 默认页数
	 */
	private int page = 1;
	/**
	 * 默认行数
	 */
	private int row = 10;
	/**
	 * 渠道id
	 */
	private String channelOid;
	/**
	 * 开始时间
	 */
	private String startTime;
	/**
	 * 结束时间
	 */
	private String endTime;
	/**
	 * 维度(日/月)--0(日),1(月)
	 */
	private int dimension;
	/**
	 * 是否包含体验金(0,否,1,是)
	 */
	private int isExperGold;
	/**
	 * 是否看环比(0,否,1,是)
	 */
	private int isRatio;
}
