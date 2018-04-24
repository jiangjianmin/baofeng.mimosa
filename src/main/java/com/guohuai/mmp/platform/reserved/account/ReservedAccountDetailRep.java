package com.guohuai.mmp.platform.reserved.account;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ReservedAccountDetailRep extends BaseRep {
	
	/**
	 * 三方支付账号
	 */
	private String payUid;

	/**
	 * 余额
	 */
	private BigDecimal balance;

	/**
	 * 累计充值总额
	 */
	private BigDecimal totalDepositAmount;

	/**
	 * 累计提现总额
	 */
	private BigDecimal totalWithdrawAmount;

	/**
	 * 超级户借款金额
	 */
	private BigDecimal superAccBorrowAmount;
	
	/**
	 * 基本户借款金额
	 */
	private BigDecimal basicAccBorrowAmount;

	/**
	 * 最近借款时间
	 */
	private Timestamp lastBorrowTime;
	/**
	 * 最近还款时间
	 */
	private Timestamp lastReturnTime;

	private Timestamp updateTime;
	private Timestamp createTime;
	
}
