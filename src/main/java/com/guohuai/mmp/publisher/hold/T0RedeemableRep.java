package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import lombok.Data;

@Data	
public class T0RedeemableRep {
	
	
	/**
	 * 产品OID
	 */
	private String productOid;
	
	/**
	 * 产品名称
	 */
	private String productName;
	
	/**
	 * 可赎回金额
	 */
	private BigDecimal redeemableHoldVolume;
	
	/**
	 * 已赎回次数
	 */
	private Integer dayRedeemCount;
	
	/**
	 * 单人单日赎回次数
	 */
	private Integer singleDayRedeemCount; 
	
	
}
