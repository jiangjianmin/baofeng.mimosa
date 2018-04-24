package com.guohuai.mmp.publisher.hold;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Data
public class MyKdbClientReq implements Serializable {

	/**
	 * 默认页数
	 */
	private int page = 1;
	/**
	 * 默认行数
	 */
	private int row = 5;
	/**
	 * 持有状态(0:全部, 1:持有中, 2:已完成)
	 */
	private int holdStatus = 0;
	/**
	 * 时间范围(yyyy-MM-dd)
	 */
	private String kdbStartDate;
	private String kdbEndDate;
	private Collection<Object> ordercodetree;
}
