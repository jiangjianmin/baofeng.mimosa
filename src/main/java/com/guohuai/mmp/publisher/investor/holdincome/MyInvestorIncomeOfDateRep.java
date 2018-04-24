package com.guohuai.mmp.publisher.investor.holdincome;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Date;

/** 我的某日收益 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MyInvestorIncomeOfDateRep {

	/** 日期 */
	private Date date;

	/** 活期收益 */
	private BigDecimal t0Income = BigDecimal.ZERO;

	/** 定期收益 */
	private BigDecimal tnIncome = BigDecimal.ZERO;

	/** 节节高收益 */
	private BigDecimal jjgIncome = BigDecimal.ZERO;

	/** 快定宝收益 */
	private BigDecimal kdbIncome = BigDecimal.ZERO;
}
