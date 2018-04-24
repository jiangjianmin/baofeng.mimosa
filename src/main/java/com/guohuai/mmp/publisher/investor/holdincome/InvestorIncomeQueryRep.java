package com.guohuai.mmp.publisher.investor.holdincome;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
public class InvestorIncomeQueryRep {

	private String oid;
	
	/**
	 * 产品CODE
	 */
	private String productCode;
	
	/**
	 * 产品名称
	 */
	private String productName;
	
	/**
	 * 收益金额
	 */
	private BigDecimal incomeAmount;
	/**
	 * 基础收益
	 */
	private BigDecimal baseAmount = BigDecimal.ZERO;
	
	/**
	 * 奖励收益
	 */
	private BigDecimal rewardAmount = BigDecimal.ZERO;
	/**
	 * 计息份额
	 */
	private BigDecimal accureVolume = BigDecimal.ZERO;
	
	/**
	 * 确认日期
	 */
    private Date confirmDate;
    
    private Timestamp createTime;
}
