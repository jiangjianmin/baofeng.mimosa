package com.guohuai.mmp.investor.baseaccount.statistics;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的首页 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyHomeQueryRep extends BaseRep {

	

	/** 累计收益总额 */
	private BigDecimal totalIncomeAmount = BigDecimal.ZERO;


	/**
	 * 资产总额
	 */
	private BigDecimal capitalAmount = BigDecimal.ZERO;

	/**
	 * 活期资产总额
	 */
	private BigDecimal t0CapitalAmount = BigDecimal.ZERO;

	/**
	 * 定期资产总额
	 */
	private BigDecimal tnCapitalAmount = BigDecimal.ZERO;
	
	/**
	 * 活期昨日收益额
	 */
	private BigDecimal t0YesterdayIncome = BigDecimal.ZERO;
	
	/**
	 * 已支付待接收
	 */
	private BigDecimal toAcceptedAmount = BigDecimal.ZERO;
	
	/**
	 * 节节高资产总额
	 */
	private BigDecimal jjgCapitalAmount = BigDecimal.ZERO;

	/**
	 * 快定宝资产总额
	 */
	private BigDecimal kdbCapitalAmount = BigDecimal.ZERO;
	
	/**
	 * 基金总额
	 */
	private BigDecimal fundCapitalAmount = BigDecimal.ZERO;
	
	/**
	 * 基金总额(含确认中)
	 */
	private BigDecimal fundCapitalAmountAll = BigDecimal.ZERO;

	/**
	 * 企业散标
	 */
	private BigDecimal scatterCapitalAmount = BigDecimal.ZERO;
	

}
