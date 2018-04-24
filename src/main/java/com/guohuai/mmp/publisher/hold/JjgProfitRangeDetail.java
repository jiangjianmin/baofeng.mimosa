package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnore;

public class JjgProfitRangeDetail {
	private String profit;
	private BigDecimal totalAmount = BigDecimal.ZERO;
	private BigDecimal remainIncome = BigDecimal.ZERO;
	
	private int startDays = 0;
	private int endDays = 0;
	
	public String getProfit() {
		return profit;
	}
	public void setProfit(String profit) {
		this.profit = profit;
	}
	public String getRange() {
		return startDays + "-" + endDays + "天";
	}
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
	@JsonIgnore
	public int getStartDays() {
		return startDays;
	}
	public void setStartDays(int startDays) {
		this.startDays = startDays;
	}
	@JsonIgnore
	public int getEndDays() {
		return endDays;
	}
	public void setEndDays(int endDays) {
		this.endDays = endDays;
	}
	@JsonIgnore
	public BigDecimal getRemainIncome() {
		return remainIncome;
	}
	public void setRemainIncome(BigDecimal remainIncome) {
		this.remainIncome = remainIncome;
	}
	
}
