package com.guohuai.mmp.publisher.investor.holdincome;

import java.math.BigDecimal;
import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**我的活期交易明细-收益*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class InvestorIncomeRep {

	
	/** 收益 */
	private BigDecimal amount;

	/** 时间 */
	private Date time;
	
	/**
	 * 产品名称
	 */
	private String productName;

}
