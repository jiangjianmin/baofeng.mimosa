package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ClosedTnDetail {
	
	/**
	 * 预计年化收益率 起始值
	 */
	private String expAror;
	
	/**
	 * 预计年化收益率 结束值
	 */
	private String expArorSec;
	
	/** 
	 * 产品ID 
	 */
	private String productOid;

	/** 
	 * 产品名称 
	 */
	private String productName;
	/**
	 * 本息金额
	 */
	private BigDecimal orderAmount;
	
	/**
	 * 收益
	 */
	private BigDecimal income;
	
	
	/**
	 * 最近一次投资时间
	 */
	private Timestamp latestOrderTime;
	
	/**
	 * 还本付息日
	 */
	private Date repayDate;
	
	/**
	 * 产品成立日
	 */
	private Date setupDate;
	
	/**
	 * 募集失败日期
	 */
	private Date raiseFailDate;
	
	private String status;
	private String statusDisp;
	private Integer relatedGuess;//1：关联竞猜宝 0：不关联竞猜宝
	
	
}
