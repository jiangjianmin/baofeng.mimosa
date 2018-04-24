package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.publisher.investor.levelincome.LevelIncomeRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的持有中活期产品 信息 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyHoldQueryRep extends BaseRep {
	
	/** 
	 * 产品ID 
	 */
	private String productOid;
	/** 
	 * 产品名称 
	 */
	private String productName;
	/**
	 * 市值
	 */
	private BigDecimal value;

	/** 昨日收益 */
	private BigDecimal yesterdayIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 累计收益 */
	private BigDecimal totalIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 总价值 */
	private BigDecimal totalValue = SysConstant.BIGDECIMAL_defaultValue;
	/** 万元收益 */
	private BigDecimal MillionIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 单笔赎回最低金额 */
	private BigDecimal minRredeem = SysConstant.BIGDECIMAL_defaultValue;
	/** 单笔赎回最高金额 */
	private BigDecimal maxRredeem = SysConstant.BIGDECIMAL_defaultValue;
	/** 单笔赎回递增金额 */
	private BigDecimal additionalRredeem = SysConstant.BIGDECIMAL_defaultValue;
	/**
	 * 单日赎回次数
	 */
	private Integer dayRedeemCount; 
	/**
	 * 产品属性：单人单日赎回次数
	 */
	private Integer singleDayRedeemCount;

	/** 投资者--今日累计赎回金额 */
	private BigDecimal dayRedeemVolume = SysConstant.BIGDECIMAL_defaultValue;
	/** 投资者--可赎回金额 */
	private BigDecimal redeemableHoldVolume = SysConstant.BIGDECIMAL_defaultValue;
	/** 产品--单人单日赎回上限 */
	private BigDecimal singleDailyMaxRedeem = SysConstant.BIGDECIMAL_defaultValue;
	/** 产品--单日赎回上限 */
	private BigDecimal dailyNetMaxRredeem = SysConstant.BIGDECIMAL_defaultValue;
	/** 产品--单日赎回上限model */
	private BigDecimal netMaxRredeemDay = SysConstant.BIGDECIMAL_defaultValue;

	/** 每万份收益 单位（元） */
	private List<MyCurrProTendencyChartRep> millionIncomeList = new ArrayList<MyCurrProTendencyChartRep>();

	/** 年化收益率走势 单位（%） */
	private List<MyCurrProTendencyChartRep> expArorList = new ArrayList<MyCurrProTendencyChartRep>();
	
	
	/** 产品分档明细 */
	private List<LevelIncomeRep> levelList = new ArrayList<LevelIncomeRep>();

	/** 奖励收益率图表 */
	private List<MyCurrProTendencyChartRep> rewardRadioList = new ArrayList<MyCurrProTendencyChartRep>();
	
	private String subType;
	/** 预计到账日期 */
	private Date payDate;
}
