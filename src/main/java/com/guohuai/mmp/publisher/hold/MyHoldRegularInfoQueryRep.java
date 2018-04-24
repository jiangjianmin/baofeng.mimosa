package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.util.Date;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的持有中定期产品详情 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyHoldRegularInfoQueryRep extends BaseRep{

	/** 预期年化收益率 */
	private BigDecimal expAror;

	/** 投资金额 */
	private BigDecimal investAmt;

	/** 预计收益 */
	private BigDecimal expectIncome;

	/** 还本付息日期 */
	private Date repayDate;

}
