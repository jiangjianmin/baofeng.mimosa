package com.guohuai.mmp.investor.baseaccount.statistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CapitalRow {
	public CapitalRow(String capitalName, String capitalColor) {
		this.capitalName = capitalName;
		this.capitalColor = capitalColor;
	}
	private String capitalColor;
	private String capitalName;
	private BigDecimal capitalAmount = BigDecimal.ZERO;
	private List<CapitalDetail> capitalDetails = new ArrayList<>();
}