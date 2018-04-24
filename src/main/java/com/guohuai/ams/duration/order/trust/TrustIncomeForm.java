package com.guohuai.ams.duration.order.trust;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * 投资标的--本息兑付订单
 * @author star.zhu
 * 2016年5月17日
 */
@Data
public class TrustIncomeForm implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 初始化数值
	 */
	public static final BigDecimal init0 	= BigDecimal.ZERO;
	public static final BigDecimal NUM100 	= new BigDecimal(100);
	public static final BigDecimal NUM10000 = new BigDecimal(10000);
	public static final BigDecimal NUM365 	= new BigDecimal(365);
	
	public TrustIncomeForm() {
		this.capital = init0;
		this.expIncomeRate = init0;
		this.expIncome = init0;
		this.incomeRate = init0;
		this.collectRate = init0;
		this.overdueRate = init0;
		this.income = init0;
	}

	// 兑付期数
	private int seq;
	// 预期收益率
	private BigDecimal expIncomeRate; 
	// 预期收益
	private BigDecimal expIncome; 
	// 实际收益率
	private BigDecimal incomeRate; 
	// 募集期收益率
	private BigDecimal collectRate; 
	// 逾期收益率
	private BigDecimal overdueRate; 
	// 实际收益
	private BigDecimal income; 
	// 本金返还
	private BigDecimal capital;
	// 本息兑付日
	private Date incomeDate;
	// 审核收益率
	private BigDecimal auditIncomeRate; 
	// 审核收益
	private BigDecimal auditIncome; 
	// 审核本金
	private BigDecimal auditCapital;
	// 确认收益率
	private BigDecimal investIncomeRate; 
	// 确认收益
	private BigDecimal investIncome; 
	// 确认本金
	private BigDecimal investCapital;
}
