package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的申请中活期产品列表 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyApplyCurrProQueryRep {

	/** 产品ID  */
	private String proOid;

	/** 产品名称 */
	private String proName;

	/** 投资金额 */
	private BigDecimal amt = SysConstant.BIGDECIMAL_defaultValue;

	/** 申请状态 */
	private String status;

}
