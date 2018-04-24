package com.guohuai.mmp.platform.baseaccount.statistics;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台首页--图表数据
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class PlatformChartQueryRep {

	/** 名称 */
	private String name;
	/** 数值 */
	private BigDecimal value = SysConstant.BIGDECIMAL_defaultValue;

}
