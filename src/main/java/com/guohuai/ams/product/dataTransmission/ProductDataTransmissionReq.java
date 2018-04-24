package com.guohuai.ams.product.dataTransmission;

import lombok.Data;

@Data
public class ProductDataTransmissionReq {
	/**
	 * 默认页数
	 */
	private int page = 1;
	/**
	 * 默认行数
	 */
	private int row = 10;
	/**
	 * 产品Id
	 */
	private String productOid;
	/**
	 * 产品全称
	 */
	private String fullName;
	/**
	 * 开始时间
	 */
	private String startTime;
	/**
	 * 结束时间
	 */
	private String endTime;
	
}
