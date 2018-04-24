package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class JjgHoldDetailRep extends BaseRep {
	private BigDecimal jjgCapitalAmount = BigDecimal.ZERO;
	private BigDecimal jjgYesterdayIncome = BigDecimal.ZERO;
	private BigDecimal totalIncomeAmount = BigDecimal.ZERO;
	private BigDecimal holdingAmount = BigDecimal.ZERO;
	private BigDecimal toConfirmAmount = BigDecimal.ZERO;
	private String productOid;
	private List<JjgProfitRangeDetail> rows = new ArrayList<>();
} 
