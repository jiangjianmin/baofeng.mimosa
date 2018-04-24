package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class HoldingT0Detail {
	/** 
	 * 产品ID 
	 */
	private String productOid;

	/** 
	 * 产品名称 
	 */
	private String productName;
	/**
	 * 市值
	 */
	private BigDecimal value;
	
	/**
	 * 昨日收益
	 */
	private BigDecimal yesterdayIncome = BigDecimal.ZERO;
	
	/**
	 * 累计收益
	 */
	private BigDecimal holdTotalIncome = BigDecimal.ZERO;
	
	/**
	 * 冻结金额
	 */
	private BigDecimal toConfirmRedeemVolume;
	
	/** 单笔赎回最低金额 */
	private BigDecimal minRredeem = SysConstant.BIGDECIMAL_defaultValue;
	
	/** 单笔赎回最高金额 */
	private BigDecimal maxRredeem = SysConstant.BIGDECIMAL_defaultValue;
	
	/** 单日净赎回上限 */
	BigDecimal netMaxRredeemDay;
	
	/** 剩余赎回金额 */
	BigDecimal dailyNetMaxRredeem;
	
	/** 单人单日赎回上限 */
	private BigDecimal singleDailyMaxRedeem;
	
	/** 产品类型 */
	private String subType;
}
