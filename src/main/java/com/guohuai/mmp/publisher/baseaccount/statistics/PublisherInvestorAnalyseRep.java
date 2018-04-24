package com.guohuai.mmp.publisher.baseaccount.statistics;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人首页-投资人质量分析
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherInvestorAnalyseRep {

	/** 规模名称 */
	private String scaleName;

	/** 规模百分比 */
	private Integer scaleCount = SysConstant.INTEGER_defaultValue;
}
