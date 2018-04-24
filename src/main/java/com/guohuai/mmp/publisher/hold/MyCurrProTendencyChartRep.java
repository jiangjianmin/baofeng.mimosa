package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的活期产品趋势图 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyCurrProTendencyChartRep {

	public MyCurrProTendencyChartRep(String axis, BigDecimal vertical) {
		this.axis = axis;
		this.vertical = vertical;
	}

	/** 纵轴 */
	private BigDecimal vertical = SysConstant.BIGDECIMAL_defaultValue;

	/** 横轴 */
	private String axis;

	/** 奖励下限日期 */
	private Integer floorday;
}
