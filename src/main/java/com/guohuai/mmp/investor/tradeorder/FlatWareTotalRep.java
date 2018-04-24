package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatWareTotalRep {
	List<FlatWareRep> flatWareRepList = new ArrayList<FlatWareRep>();
	BigDecimal accruableHoldVolume = BigDecimal.ZERO;
	
	BigDecimal tHoldVolume = BigDecimal.ZERO;
	BigDecimal tIncomeAmount = BigDecimal.ZERO;
}
