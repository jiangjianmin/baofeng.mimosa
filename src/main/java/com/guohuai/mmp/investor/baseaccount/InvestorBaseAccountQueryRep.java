package com.guohuai.mmp.investor.baseaccount;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class InvestorBaseAccountQueryRep {
	/**
	 * UUID
	 */
	private String oid;
	
	/**
	 * 用户OID
	 */
	private String userOid;
	/**
	 * 用户 账号
	 */
	private String phoneNum;
	/**
	 * 用户名
	 */
	private String realName;
	/**
	 * 状态
	 */
	private String status;
	private String statusDisp;
	
	/**
	 * 账户所有者
	 */
	private String owner;
	private String ownerDisp;
	
	/**
	 * 账户余额
	 */
	private BigDecimal balance;
	/**
	 * 累计投资
	 */
	private BigDecimal totalInvestAmount;
	/**
	 * 累计收益
	 */
	private BigDecimal totalIncomeAmount;
	
	/**
	 * 注册时间
	 */
	private Timestamp createTime;
}
