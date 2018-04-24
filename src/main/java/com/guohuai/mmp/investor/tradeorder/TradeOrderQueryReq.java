package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TradeOrderQueryReq {

	private String orderType;
	private String orderStatus;

	private String isAuto;

	private String channelOid;

	private String channelName;
	
	private String orderCode;

	private String createTimeBegin;
	
	private String createTimeEnd;
	 
	private BigDecimal minOrderAmount;
	private BigDecimal maxOrderAmount;
	private String createMan;
	private String productName;
	private String productType;
	private String productOid;
	private String investorOid;

	private String publisherClearStatus;
	private String investorOffsetOid;
	private String publisherOffsetOid;
	private String phoneNum;
	private String order;
	private String sort;
	int page;
	int rows;
}
