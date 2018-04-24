package com.guohuai.mmp.publisher.investor.holdincome;

import java.math.BigDecimal;
import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的收益明细 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MyInvestorIncomeDetailsRep {
	
	public MyInvestorIncomeDetailsRep(InvestorIncomeEntity entity){
		this.time = entity.getConfirmDate();
		this.proName = entity.getProduct().getName();
		this.amount = entity.getIncomeAmount();
	}

	/** 时间 */
	private Date time;
	/** 产品名称 */
	private String proName;
	/** 收益 */
	private BigDecimal amount;

}
