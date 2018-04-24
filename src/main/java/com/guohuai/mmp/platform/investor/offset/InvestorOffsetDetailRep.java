package com.guohuai.mmp.platform.investor.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestorOffsetDetailRep extends BaseRep {
	
	
	
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
