package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.util.Date;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的已结清定期产品列表 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyClosedRegularProQueryRep {

	/** 产品ID */
	private String proOid;

	/** 产品名称 */
	private String proName;

	/** 投资金额 */
	private BigDecimal investAmt = SysConstant.BIGDECIMAL_defaultValue;

	/** 购买日期 */
	private Date buyDate;

	/** 还本付息日 */
	private Date repayDate;

	/** 退款日（此字段非空表示退款产品，为空表示正常结清产品） */
	private Date refundDate;

	/** 预计退款日 */
	private String toRefundDate;

	/** 总收益 */
	private BigDecimal totalIncome = SysConstant.BIGDECIMAL_defaultValue;

	/** 状态(closed:已结算，refunded:已退款,refunding:退款中) */
	private String status;
}
