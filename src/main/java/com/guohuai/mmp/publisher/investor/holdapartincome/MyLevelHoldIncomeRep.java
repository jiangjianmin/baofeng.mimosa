package com.guohuai.mmp.publisher.investor.holdapartincome;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的活期奖励收益详情页 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class MyLevelHoldIncomeRep {

	/** 投资金额 */
	private BigDecimal investAmt;

	/** 当前金额 */
	private BigDecimal value;

	/** 投资日期 */
	private Date investDate;

	/** 升档时间 */
	private Date upLevelDate;

}
