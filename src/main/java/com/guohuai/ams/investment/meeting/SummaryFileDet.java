package com.guohuai.ams.investment.meeting;

import lombok.Data;

/**
 * 过会纪要详情req
 * 
 * @author Administrator
 *
 */
@Data
public class SummaryFileDet {

	/**
	 * 文件名
	 */
	private String name;
	/**
	 * 文件路径
	 */
	private String url;
	/**
	 * 大小
	 */
	private long size;
}
