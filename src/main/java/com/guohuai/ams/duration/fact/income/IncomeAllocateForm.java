package com.guohuai.ams.duration.fact.income;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeAllocateForm implements Serializable {

	private static final long serialVersionUID = 1539925102915297056L;
	
	@NotBlank
	public String assetpoolOid;
	@NotBlank
	public String incomeDistrDate;//收益分配日
	@NotBlank
	public String productTotalScale;//产品总规模
	@NotBlank
	public String productRewardBenefit;//奖励收益
	@NotBlank
	public String productDistributionIncome;//分配收益
	@NotBlank
	public String productAnnualYield;//年化收益率

}
