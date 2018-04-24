package com.guohuai.mmp.platform.investor.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestorOffsetQueryRep {
	
	/**
	 * 轧差OID
	 */
	String offsetOid;
	
	/**
	 * 轧差日期
	 */
	private Date offsetDate;
	
	/**
	 * 轧差批次
	 */
	private String offsetCode;
	
	
	/**
	 * 清算状态
	 */
	private String clearStatus;
	private boolean isClearTimeArr;
	private String clearStatusDisp;
	
	/**
	 * 结算状态
	 */
	private String closeStatus;
	private String closeStatusDisp;
	
	/**
	 * 赎回金额
	 */
	private BigDecimal redeemAmount;
	
	/**
	 * 结算人
	 */
	private String closeMan;
	
	/**
	 * 逾期状态
	 */
	Timestamp createTime;
	Timestamp updateTime;
	
	
	
}
