package com.guohuai.ams.investment;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseResp;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class InvestmentDetResp extends BaseResp {

	public InvestmentDetResp(Investment investment) {
		super();
		// 资产规模 元转万
		if (investment.getRaiseScope() != null) {
			BigDecimal yuan = investment.getRaiseScope().divide(new BigDecimal(10000));
			investment.setRaiseScope(yuan);
		}
		// 起购金额 元转万
		if (investment.getFloorVolume() != null) {
			BigDecimal yuan = investment.getFloorVolume().divide(new BigDecimal(10000));
			investment.setFloorVolume(yuan);
		}
		// 预计年化收益 小数转百分比
		if (investment.getExpAror() != null) {
			BigDecimal percentage = investment.getExpAror().multiply(new BigDecimal(100));
			investment.setExpAror(percentage);
		}
		// 逾期收益 小数转百分比
		if (investment.getOverdueRate() != null) {
			BigDecimal percentage = investment.getOverdueRate().multiply(new BigDecimal(100));
			investment.setOverdueRate(percentage);
		}
		// 募集期收益 小数转百分比
		if (investment.getCollectIncomeRate() != null) {
			BigDecimal percentage = investment.getCollectIncomeRate().multiply(new BigDecimal(100));
			investment.setCollectIncomeRate(percentage);
		}
		// 授信额度 元转万
		if (investment.getTrustAmount() != null) {
			BigDecimal trustAmount = investment.getTrustAmount().divide(new BigDecimal(10000));
			investment.setTrustAmount(trustAmount);
		}
		// 剩余授信额度 元转万
		if (investment.getRestTrustAmount() != null) {
			BigDecimal restTrustAmount = investment.getRestTrustAmount().divide(new BigDecimal(10000));
			investment.setRestTrustAmount(restTrustAmount);
		}
		this.investment = investment;
	}

	private Investment investment;
}
