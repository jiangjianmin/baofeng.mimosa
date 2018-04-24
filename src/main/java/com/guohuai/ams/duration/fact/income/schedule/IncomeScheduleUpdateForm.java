package com.guohuai.ams.duration.fact.income.schedule;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeScheduleUpdateForm implements Serializable {
	
	private static final long serialVersionUID = -6094434277650899872L;

	
	@NotBlank
	public String oid;
//	@NotBlank
//	public String incomeDistrDate;//收益分配日
	@NotBlank
	public String productAnnualYield;//年化收益率

}
