package com.guohuai.ams.product;

import lombok.Data;

@Data
public class ProfitDurationReq {

	/**
	 * 默认页数
	 */
	private int page = 1;
	
	/**
	 * 默认行数
	 */
	private int row = 10;
	
	/**
	 * 产品名称
	 */
	private String productName;
	
	/**
	 * 产品类型（开放式、封闭式 ）
	 */
	private String productType;
	
	/**
	 * 产品状态
	 */
	private String productStatus;
	
	
	/**
	 * 创建开始时间
	 */
	private String raiseTimeBegin;
	
	/**
	 * 创建结束时间
	 */
	private String raiseTimeEnd;
	
	/**
	 * 派息审核状态
	 */
	private String auditStatus;
}
