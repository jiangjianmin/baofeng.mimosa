package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的申请中定期产品列表 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyApplyRegularProQueryRep {

	/** 产品ID */
	private String proOid;

	/** 产品名称 */
	private String proName;

	/** 已受理金额 */
	private BigDecimal acceptedAmt = SysConstant.BIGDECIMAL_defaultValue;

	/** 待受理金额 */
	private BigDecimal toAcceptAmt = SysConstant.BIGDECIMAL_defaultValue;

	/** 申请状态(toConfirm待确认，holding持有中，refunding退款中，) */
	private String applyStatus;

	/** 退款中金额(退款中时显示) */
	private BigDecimal reAmount = SysConstant.BIGDECIMAL_defaultValue;
}
