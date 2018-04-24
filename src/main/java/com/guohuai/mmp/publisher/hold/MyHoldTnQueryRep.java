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
public class MyHoldTnQueryRep extends BaseRep {
	
	
	/**
	 * 定期资产总额
	 */
	private BigDecimal tnCapitalAmount = BigDecimal.ZERO;
	/**
	 * 定期昨日收益额
	 */
	private BigDecimal tnYesterdayIncome = BigDecimal.ZERO;
	
	/** 累计收益总额 */
	private BigDecimal totalIncomeAmount = BigDecimal.ZERO;
	
	/**
	 * 持有中
	 */
	Pages<HoldingTnDetail> holdingTnDetails;
	
//	/**
//	 * 申请中
//	 */
//	Pages<ToConfirmTnDetail> toConfirmTnDetails;
	
	/**
	 * 已结清
	 */
	Pages<ClosedTnDetail> closedTnDetails;
	
}
