package com.guohuai.mmp.investor.tradeorder;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class InvestOrRedeemDetailRep {
	
	String phoneNum;//所属投资人账号
	
	String realName;//所属投资人

	/** 所属发行人 */
	String publisherOid;

	/** 所属产品 */
	String name;

	/**所属渠道 **/
	String channelOid;

	/** 所属投资人轧差 */
	String investorOffsetOid;

	/** 所属发行人轧差 */
	String publisherOffsetOid;
	
   /**所属三方对账**/
	String checkOid;
	
	/** 所属持有人手册 **/ 
	String holdOid;
	
	String orderCode;
	
	String orderType;
	
	String orderAmount;
	
	String orderVolume;
	
	String payAmount;
	
	String couponAmount;
	
	String coupons;
	
	String couponType;
	
	String payStatus;
	
	String acceptStatus;
	
	String refundStatus;
	
	String orderStatus;
	
	String checkStatus;
	
	String contractStatus;
	
	String createMan;
	
	String orderTime;
	
	String completeTime;
	
	String publisherClearStatus;
	
	String publisherConfirmStatus;
	
	String publisherCloseStatus;
	
	String investorClearStatus;
	
	String investorCloseStatus;
	
	String holdVolume;
	
	String redeemStatus;
	
	String accrualStatus;
	
	String beginAccuralDate;
	
	String corpusAccrualEndDate;
	
	String beginRedeemDate;
	
	String totalIncome;
	
	String totalBaseIncome;
	
	String totalRewardIncome;
	
	String yesterdayBaseIncome;
	
	String yesterdayRewardIncome;
	
	String yesterdayIncome;
	
	String toConfirmIncome;
	
	String incomeAmount;
	
	String expectIncomeExt;
	
	String expectIncome;
	
	String value;
	
	String confirmDate;
	
	String holdStatus;
	
	String province;
	
	String city;
	
	String updateTime;
	
	String createTime;
}
