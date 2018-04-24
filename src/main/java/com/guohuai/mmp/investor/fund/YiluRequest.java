package com.guohuai.mmp.investor.fund;

import java.io.Serializable;

@lombok.Data
public class YiluRequest implements Serializable{

	/**
	 * 一路财富我的基金请求参数
	 */
	private static final long serialVersionUID = 9038155102596187592L;
	
	//渠道编号
	private String partnerid;
	//业务代码
	private String businesscode;
	//流水号
	private String custReqSerialno;
	//渠道服务器时间
	private String localtime;
	//渠道用户唯一标识
	private String openuid;
	//是否显示总额
	private String showfundWealth;
	//是否显示在持基金列表
	private String showfundList;
	//是否显示本金
	private String showtotalPrincipal;
	//是否显示昨日收益
	private String showfundYestTotalProfit;
	//是否显示总盈亏
	private String showfundprofit;
	//签名
	private String sign;
	

}
