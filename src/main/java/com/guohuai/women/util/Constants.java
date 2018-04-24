package com.guohuai.women.util;

import java.sql.Timestamp;

public interface Constants {
	
	/**
	 * 活动开始时间
	 */
	public static final Timestamp START_DATE_TIME= DateUtil.string2Time("2017-03-08 00:00:00");
	
	/**
	 * 活动结束时间
	 */
	public static final Timestamp END_DATE_TIME= DateUtil.string2Time("2017-03-08 23:59:59");

}
