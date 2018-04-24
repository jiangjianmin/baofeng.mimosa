package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的持有中定期产品详情 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TnHoldingDetail extends BaseRep {

	/**
	 * 投资额
	 */
	private BigDecimal investVolume;

	/**
	 * 预计回款金额 // 实际回款金额
	 */
	private BigDecimal payAmount;

	/**
	 * 预计收益开始区间
	 */
	private BigDecimal expectIncome;

	/**
	 * 预计收益结束区间
	 */
	private BigDecimal expectIncomeExt;

	/**
	 * 预期年化收益率 开始
	 */
	private String expAror;

	/**
	 * 预期年化收益率 结束
	 */
	private String expArorExt;

	/**
	 * 募集开始日期
	 */
	private Date raiseStartDate;

	/**
	 * 募集结束日期
	 */
	private Date raiseEndDate;

	/**
	 * 存续期开始日期
	 */
	private Date setupDate;

	/**
	 * 存续期结束日期
	 */
	private Date durationPeriodEndDate;

	/**
	 * 还本付息日期
	 */
	private Date repayDate;

	/**
	 * 最近一次投资时间
	 */
	private Timestamp latestOrderTime;
	/**
	 * 累计收益
	 */
	private BigDecimal holdTotalIncome;
	
	/**
	 * 持仓状态
	 */
	private String holdStatus;

}
