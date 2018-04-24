package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderHoldDaysDetail {
	private int holdDays = 0;
	private BigDecimal holdVolume = BigDecimal.ZERO;
	private BigDecimal totalIncome = BigDecimal.ZERO;
}
