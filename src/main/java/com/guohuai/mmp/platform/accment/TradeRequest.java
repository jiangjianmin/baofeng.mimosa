package com.guohuai.mmp.platform.accment;

import java.math.BigDecimal;


import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 交易 用于申购、赎回
 * @author yuechao
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TradeRequest {
	
	/**
	 * 会员ID
	 */
	private String userOid;
	
	/**
	 * 用户类型
	 */
	private String userType;
	
	/**
	 * 交易类别
	 */
	private String orderType;
	
	/**
	 * 关联产品编号
	 */
	private String relationProductNo;
	
	/**
	 * 产品类型
	 */
	private String productType;
	
	/**
	 * 交易额
	 */
	private BigDecimal balance;
	
	/**
	 * 交易用途
	 */
	private String remark;
	
	/**
	 * 定单号
	 */
	private String orderNo;
	
	
}
