package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.component.web.view.BaseRep;

import lombok.Data;

@Data
public class TradeDetailReq extends BaseRep{
	/**
	 * 默认页数
	 */
	private int page = 1;
	/**
	 * 默认行数
	 */
	private int row = 30;
	/**
	 * 订单号
	 */
	private String orderCode;
	/**
	 * 交易类型（0 全部，1 投资，2提现，3回款，4体验金收益）
	 */
	private int tradeType = 0;
	/**
	 * 交易开始时间
	 */
	private String orderTimeStart;
	/**
	 * 交易结束时间
	 */
	private String orderTimeEnd;
}
