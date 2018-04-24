package com.guohuai.mmp.publisher.investor.levelincome;

import java.io.Serializable;
import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.Data;

@Data
public class LevelIncomeRep implements Serializable {

	private static final long serialVersionUID = -1922988492844862611L;
	/** 奖励收益率 */
	private BigDecimal ratio = SysConstant.BIGDECIMAL_defaultValue;
	/** 起始日 */
	private Integer startDate;
	/** 结束日 */
	private Integer endDate;
	/** 最新市值 */
	private BigDecimal value = SysConstant.BIGDECIMAL_defaultValue;
	/** 收益金额 */
	private BigDecimal incomeAmount = SysConstant.BIGDECIMAL_defaultValue;
	/** 阶段 */
	private String level;
}
