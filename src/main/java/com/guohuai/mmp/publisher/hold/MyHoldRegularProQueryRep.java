package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.util.Date;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的持有中定期产品列表 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyHoldRegularProQueryRep {

	/** 产品ID */
	private String proOid;

	/** 产品名称 */
	private String proName;

	/** 预期年化收益率 */
	private BigDecimal expAror;

	/** 投资金额 */
	private BigDecimal investAmt = SysConstant.BIGDECIMAL_defaultValue;

	/** 收益方式 */
	private String incomeTyp;

	/** 到期日 */
	private Date dDate;

	/**
	 * 持仓状态(holding持有中，closing:结算中)
	 * 
	 * @see PublisherHoldEntity
	 */
	private String status;

	/** 结算中金额(结算中时显示) */
	private BigDecimal cloAmount = SysConstant.BIGDECIMAL_defaultValue;
}
