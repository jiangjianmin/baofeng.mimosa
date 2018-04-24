package com.guohuai.ams.product;

import java.io.Serializable;

import lombok.ToString;

@ToString
public class MimosaSDKReq implements Serializable {

	private static final long serialVersionUID = 610820698397903078L;

	/** 页号 */
	private int page;

	/** 分页大小,0代表查询所有 */
	private int rows;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

}
