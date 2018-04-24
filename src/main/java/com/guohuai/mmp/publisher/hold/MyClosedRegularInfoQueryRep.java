package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.util.Date;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的已结清定期产品详情 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyClosedRegularInfoQueryRep extends BaseRep {

	/** 总期限 */
	private int dayNum;
	/** 投资金额 */
	private BigDecimal investAmt;
	/** 累计收益 */
	private BigDecimal totalIncome;
	/** 计息开始日 */
	private Date sDate;
	/** 计息截止日 */
	private Date eDate;

}
