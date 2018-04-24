package com.guohuai.mmp.investor.baseaccount.statistics;

import java.math.BigDecimal;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的资产（账户余额，活期总资产，定期总资产） */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyCaptialQueryRep extends BaseRep {

	/**
	 * 资产总额
	 */
	private BigDecimal capitalAmount = BigDecimal.ZERO;

	/**
	 * 活期资产总额
	 */
	private BigDecimal t0CapitalAmount = BigDecimal.ZERO;
	private List<CapitalDetail> t0CapitalDetails;

	/**
	 * 定期资产总额
	 */
	private BigDecimal tnCapitalAmount = BigDecimal.ZERO;
	private List<CapitalDetail> tnCapitalDetails;

	/** 申请中资产 */
	private BigDecimal applyAmt = BigDecimal.ZERO;
	private List<CapitalDetail> applyCapitalDetails;
	
	/** 体验金总资产 */
	private BigDecimal experienceCouponAmount = BigDecimal.ZERO;
	
	/**
	 * 已支付待接收
	 */
	private BigDecimal toAcceptedAmount = BigDecimal.ZERO;
	
	
	
}
