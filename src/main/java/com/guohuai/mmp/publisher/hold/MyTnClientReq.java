package com.guohuai.mmp.publisher.hold;

import com.guohuai.component.web.view.BaseRep;

import lombok.Data;

@Data
public class MyTnClientReq extends BaseRep{
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
	private String tnStartDate;
	private String tnEndDate;
}
