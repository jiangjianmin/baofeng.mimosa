package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublisherHoldView {

	public PublisherHoldView(PublisherHoldEntity h) {
		this.oid = h.getOid();
		//this.investorOid = h.getInvestorOid();
		if (null != h.getProduct()) {
			this.productOid = h.getProduct().getOid();
			this.productName = h.getProduct().getName();
		}
	//	this.totalHoldVolume = h.getTotalHoldVolume();
		this.investTotalVolume = h.getTotalInvestVolume();
		this.totalBaseIncome = h.getTotalBaseIncome();
		this.totalRewardIncome = h.getTotalRewardIncome();
		this.yesterdayBaseIncome = h.getYesterdayBaseIncome();
		this.yesterdayRewardIncome = h.getYesterdayRewardIncome();
		this.holdYesterdayIncome = h.getHoldYesterdayIncome();
	}

	private String oid;
	private String investorOid;
	private String productOid;
	private String productName;
	private BigDecimal totalHoldVolume;
	private BigDecimal investTotalVolume;
	private BigDecimal totalBaseIncome;
	private BigDecimal totalRewardIncome;
	private BigDecimal incomeAmount;
	private BigDecimal yesterdayBaseIncome;
	private BigDecimal yesterdayRewardIncome;
	private BigDecimal holdYesterdayIncome;

}
