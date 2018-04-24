package com.guohuai.mmp.publisher.baseaccount.statistics;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人首页-在售产品募集进度
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherRaiseRateRep {

	/** 产品名称 */
	private String proName;

	/** 总规模 */
	private BigDecimal total = SysConstant.BIGDECIMAL_defaultValue;

	/** 已募集 */
	private BigDecimal raised = SysConstant.BIGDECIMAL_defaultValue;
	
	/** 还需募集 */
	private BigDecimal toRaised = SysConstant.BIGDECIMAL_defaultValue;
}
