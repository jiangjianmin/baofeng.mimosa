package com.guohuai.mmp.investor.baseaccount.statistics;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CapitalDetail {
	public CapitalDetail(String productName) {
		this.productName = productName;
	}
	private BigDecimal amount = BigDecimal.ZERO;
	private String productName;

}