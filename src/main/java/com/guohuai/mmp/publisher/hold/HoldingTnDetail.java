package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class HoldingTnDetail {
	
	/**
	 * 预计年化收益率 起始值
	 */
	private String expAror;
	
	/**
	 * 预计年化收益率 结束值
	 */
	private String expArorSec;
	
	/**
	 * 预计收益
	 */
	private BigDecimal expectIncome;
	
	/**
	 * 预计收益
	 */
	private BigDecimal expectIncomeExt;
	/**
	 * 最近一次投资时间
	 */
	private Timestamp latestOrderTime;
	
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
	private BigDecimal orderAmount;
	
	/**
	 * 产品成立日
	 */
	private Date setupDate;
	
	/**
	 * 存续期结束 (即到期日)
	 */
	private Date durationPeriodEndDate;
	
	/**
	 * 还本付息日(到账日)
	 */
	private Date repayDate;
	
	private String status;
	private String statusDisp;
	private Integer relatedGuess;//1：关联竞猜宝 0：不关联竞猜宝
}
