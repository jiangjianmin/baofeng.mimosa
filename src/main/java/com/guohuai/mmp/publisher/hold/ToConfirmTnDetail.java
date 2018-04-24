package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ToConfirmTnDetail {
	
	/**
	 * 预计年化收益率 起始值
	 */
	private BigDecimal expAror;
	
	/**
	 * 预计年化收益率 结束值
	 */
	private BigDecimal expArorSec;
	
	/**
	 * 预计收益
	 */
	private BigDecimal expectIncome;
	
	/**
	 * 预计收益
	 */
	private BigDecimal expectIncomeExt;
	
	/**
	 * 产品OID
	 */
	private String productOid;
	
	/** 
	 * 产品名称 
	 */
	private String productName;
	
	/**
	 * 投资额
	 */
	private BigDecimal orderAmount;
	
	/**	
	 * 产品成立日
	 */
	private Date setupDate;
	
	/**
	 * 还本付息日
	 */
	private Date repayDate;
	
	/**
	 * 最近一次投资时间
	 */
	private Timestamp latestOrderTime;
	
	
	private String status;
	
	private String statusDisp;
	
}
