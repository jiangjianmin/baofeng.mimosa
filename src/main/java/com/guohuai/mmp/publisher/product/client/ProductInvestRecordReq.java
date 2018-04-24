package com.guohuai.mmp.publisher.product.client;

import lombok.Data;

@Data
public class ProductInvestRecordReq {
	
	/**
	 * 默认页数
	 */
	private int page = 1;
	/**
	 * 默认行数
	 */
	private int row = 10;
	/**
	 * 产品Oid
	 */
	private String productOid;
}
