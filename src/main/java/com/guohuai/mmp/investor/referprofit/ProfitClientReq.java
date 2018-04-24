package com.guohuai.mmp.investor.referprofit;

import com.guohuai.component.web.view.BaseRep;

import lombok.Data;

@Data
public class ProfitClientReq extends BaseRep{
	/**
	 * 默认页数
	 */
	private int page = 1;
	/**
	 * 默认行数
	 */
	private int row = 10;
	/**
	 * 结算状态(-1:全部, 0:未结算, 1:已结算)
	 */
	private int status = -1;
	/**
	 * 生成日期(-1:全部, 0:7天, 1:一个月, 2:三个月)
	 */
	private int timeDemision = -1;
	/**
	 * 0:当日排行, 1:本周排行, 2:本月排行
	 */
	private int rank;
}
