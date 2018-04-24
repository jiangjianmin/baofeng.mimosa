package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.publisher.investor.levelincome.LevelIncomeRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**我的活期*/
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyApplyQueryRep extends BaseRep {

	/** 昨日收益  */
	private BigDecimal yesterdayIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 累计收益 */
	private BigDecimal totalIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 当前市值 */
	private BigDecimal totalValue = SysConstant.BIGDECIMAL_defaultValue;

	private List<LevelIncomeRep> levelList = new ArrayList<LevelIncomeRep>();
}
