package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.Pages;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的持有中活期产品列表 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyHoldT0QueryRep extends BaseRep {
	
	
	/**
	 * 活期资产总额
	 */
	private BigDecimal t0CapitalAmount = BigDecimal.ZERO;
	/**
	 * 活期昨日收益额
	 */
	private BigDecimal t0YesterdayIncome = BigDecimal.ZERO;
	
	/** 累计收益总额 */
	private BigDecimal totalIncomeAmount = BigDecimal.ZERO;
	
	/**
	 * 持有中
	 */
	Pages<HoldingT0Detail> holdingDetails;
	
	/**
	 * 申请中
	 */
	Pages<ToConfirmT0Detail> toConfirmDetails;
	
	/**
	 * 可赎回产品OID
	 */
	private String productOid;
	
}
