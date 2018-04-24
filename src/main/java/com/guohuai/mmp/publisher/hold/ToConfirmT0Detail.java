package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ToConfirmT0Detail {
	
	/** 
	 * 产品ID 
	 */
	private String productOid;

	/** 
	 * 产品名称 
	 */
	private String productName;
	
	/**
	 * 投资金额
	 */
	private BigDecimal toConfirmInvestVolume;
	
	/** 单日净赎回上限 */
	BigDecimal netMaxRredeemDay;
	
	/** 剩余赎回金额 */
	BigDecimal dailyNetMaxRredeem;
	
	/** 单人单日赎回上限 */
	private BigDecimal singleDailyMaxRedeem;
	
	/** 产品类型 */
	private String subType;
}
