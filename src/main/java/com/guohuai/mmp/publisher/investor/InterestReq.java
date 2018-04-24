package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;
import java.sql.Date;

import com.guohuai.ams.product.Product;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class InterestReq {
	/**
	 * 产品
	 */
	private Product product;
	
	/**
	 * 收益
	 */
	private BigDecimal incomeAmount;
	
	/**
	 * 可计息份额
	 */
	private BigDecimal totalInterestedVolume;
	
	/**
	 * 利率
	 */
	private BigDecimal ratio;
	
	/**
	 * 收益日期
	 */
	private Date incomeDate;
	
	/**
	 * 收益类型
	 */
	private String incomeType;

}
