package com.guohuai.mmp.investor.referprofit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class ProfitRuleForm implements Serializable{
	
	private static final long serialVersionUID = 6927739962991685215L;
	
	protected String oid;
	/**
	 * 一级邀请人奖励比例
	 */
	private BigDecimal firstFactor;
	/**
	 * 二级邀请人奖励比例
	 */
	private BigDecimal secondFactor;
	/**
	 * 投资人奖励比例
	 */
	private BigDecimal investorFactor;
	/**
	 * 活期奖励比例
	 */
	private BigDecimal demandFactor;
	/**
	 * 定期奖励比例
	 */
	private BigDecimal depositFactor;
	/**
	 * 修改时间
	 */
	private Date updateTime;
	/**
	 * 创建时间
	 */
	private Date createTime;
}
