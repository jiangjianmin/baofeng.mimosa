package com.guohuai.mmp.investor.referprofit;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProfitProvideTotalRep extends BaseRep {
	
	/**
	 * 当前已发放总金额
	 */
	private BigDecimal providedTotalAmout = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 当前待发放总金额
	 */
	private BigDecimal toProvideTotalAmout = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 平台累计奖励人数
	 */
	private Integer providedTotalNums = SysConstant.INTEGER_defaultValue;

}
