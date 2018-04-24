package com.guohuai.mmp.publisher.investor.holdapartincome;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的活期奖励收益详情页 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class HoldApartIncomeQueryRep {

	private String phoneNum;
	
	/**
	 * 确认日期
	 */
	private Date confirmDate;
	/**
	 * 计息份额
	 */
	private BigDecimal accureVolume;
	
	/**
	 * 收益金额
	 */
	private BigDecimal incomeAmount;
	
	/**
	 * 基础收益
	 */
	private BigDecimal baseAmount;
	
	/**
	 * 奖励金额
	 */
	private BigDecimal rewardAmount;

}
