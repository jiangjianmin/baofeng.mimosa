package com.guohuai.mmp.investor.fund;

import java.io.Serializable;
import java.math.BigDecimal;

@lombok.Data
public class YiluResponse implements Serializable {

	/**
	 * 一路财富返回参数
	 */
	private static final long serialVersionUID = -1146439892945910125L;
	
	//响应状态
	private String respstat;
	//响应文字
	private String respmsg;
	//账户总额
	private BigDecimal fundWealth;
	//本金
	private BigDecimal totalPrincipal;
	//昨日收益
	private BigDecimal fundYestTotalProfit;
	//总盈亏
	private BigDecimal fundprofit;
	//基金代码
	private String fundcode;
	//确认日期
	private String transactioncfmdate;
	//基金名称
	private String fundname;
	//基金类型
	private String fundtype;
	//银行卡号
	private String depositacct;
	//可用份额
	private BigDecimal availablevol;
	//冻结份额
	private BigDecimal frozevol;
	//市值
	private BigDecimal minredemptionvol;
	//最新净值
	private BigDecimal nav;
	//上个交易日收益
	private BigDecimal yestDprofit;
	//买入成本
	private BigDecimal buyincost;
	//持仓成本
	private BigDecimal holdcost;
	//基金状态
	private String fundstatus;
	//总计金额
	private BigDecimal totalmount;
	//总计基金数
	private BigDecimal totalno;
	//基金公司
	private String taname;
	//基金风险等级
	private String fundriskgrade;
	//基金风险等级说明
	private String fundriskgradedesc;
	//本金
	private BigDecimal principal;
	//是否可赎回
	private String canRedeem;
	//单只收益
	private BigDecimal profit;
	//分红方式
	private String specifyredeemflag;
	//浮动盈亏
	private BigDecimal applicationvol;
}
