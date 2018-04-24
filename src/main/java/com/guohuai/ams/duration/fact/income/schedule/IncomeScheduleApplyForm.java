package com.guohuai.ams.duration.fact.income.schedule;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeScheduleApplyForm implements Serializable {

	private static final long serialVersionUID = 1539925102915297056L;
	
	@NotBlank
	public String assetpoolOid;
	@NotBlank
	public String incomeDistrDate;//收益分配日
	@NotBlank
	public String productAnnualYield;//年化收益率

}
