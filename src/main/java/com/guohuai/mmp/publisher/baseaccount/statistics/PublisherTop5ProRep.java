package com.guohuai.mmp.publisher.baseaccount.statistics;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人首页-销售TOP5产品
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherTop5ProRep {

	/** 产品名称 */
	private String proName;

	/** 投资金额 */
	private BigDecimal investAmt = SysConstant.BIGDECIMAL_defaultValue;

}
