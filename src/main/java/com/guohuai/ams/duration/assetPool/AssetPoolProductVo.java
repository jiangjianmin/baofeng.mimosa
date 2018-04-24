package com.guohuai.ams.duration.assetPool;

import lombok.Data;

@Data
public class AssetPoolProductVo {

	private String productOid;
	private String productCode;
	private String productName;
	private String productType;
	private String durationPeriodDays;
	private String collectedVolume;
	private String assetPoolName;
	private String noPayInvest;
	private String payInvest;
	private String raiseStartDate;
	private String repayDate;
	private String repayLoanStatus;
	private String repayInterestStatus;
	private String productCash;
	private String productInvest;
	private String productIncome;
	private String productState;
	private String createTime;
	private String creator;
	
}
