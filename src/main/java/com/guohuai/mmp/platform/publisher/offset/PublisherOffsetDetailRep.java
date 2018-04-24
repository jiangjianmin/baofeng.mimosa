package com.guohuai.mmp.platform.publisher.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherOffsetDetailRep extends BaseRep {
	
	
	
	/**
	 * 轧差日期
	 */
	private Date offsetDate;
	
	/**
	 * 轧差批次
	 */
	private String offsetCode;
	
	/**
	 * 净头寸
	 */
	private BigDecimal netPosition;
	
	/**
	 * 清算状态
	 */
	private String clearStatus;
	private String clearStatusDisp;
	
	/**
	 * 交收状态
	 */
	private String confirmStatus;
	private String confirmStatusDisp;
	
	/**
	 * 结算状态
	 */
	private String closeStatus;
	private String closeStatusDisp;
	
	/**
	 * 申购金额
	 */
	private BigDecimal buyAmount;
	/**
	 * 赎回金额
	 */
	private BigDecimal redeemAmount;
	
	/**
	 * 结算人
	 */
	private String closeMan;
	
	String spvOid;
	String spvName;
	
	Timestamp createTime;
	Timestamp updateTime;
	
	
}
