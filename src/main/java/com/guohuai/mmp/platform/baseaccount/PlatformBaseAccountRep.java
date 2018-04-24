package com.guohuai.mmp.platform.baseaccount;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class PlatformBaseAccountRep {

	/**
	 * 余额
	 */
	private BigDecimal balance;
	
	/**
	 * 超级户借款金额
	 */
	private BigDecimal superAccBorrowAmount;
	
	/**
	 * 状态
	 */
	private String status;
	private String statusDisp;
	
	/**
	 * 累计交易总额
	 */
	private BigDecimal totalTradeAmount;
	
	
	/**
	 * 累计借款总额
	 */
	private BigDecimal totalLoanAmount;
	
	/**
	 * 累计还款总额
	 */
	private BigDecimal totalReturnAmount;
	
	
	/**
	 * 累计付息总额
	 */
	private BigDecimal totalInterestAmount;
	
	
	
	
	/**
	 * 投资人充值总额
	 */
	private BigDecimal investorTotalDepositAmount;
	
	/**
	 * 投资人提现总额
	 */
	private BigDecimal investorTotalWithdrawAmount;
	
	/**
	 * 发行人充值总额
	 */
	private BigDecimal publisherTotalDepositAmount;
	
	/**
	 * 发行人提现总额
	 */
	private BigDecimal publisherTotalWithdrawAmount;
	
	/**
	 * 注册投资人数
	 */
	private Integer registerAmount;
	
	/**
	 * 投资人数
	 */
	private Integer investorAmount;
	
	/**
	 * 持仓人数
	 */
	private Integer investorHoldAmount;
	
	/**
	 * 逾期次数
	 */
	private Integer overdueTimes;
	
	/**
	 * 发行产品数
	 */
	private Integer productAmount;
	
	/**
	 * 已结算产品数
	 */
	private Integer closedProductAmount;
	
	/**
	 * 待结算产品数
	 */
	private Integer toCloseProductAmount;
	
	/**
	 * 在售产品数
	 */
	private Integer onSaleProductAmount;
	
	/**
	 * 发行人数
	 */
	private Integer publisherAmount;
	
	/**
	 * 实名投资人数
	 */
	private Integer verifiedInvestorAmount;
	
	/**
	 * 活跃投资人数
	 */
	private Integer activeInvestorAmount;
	
	private Timestamp updateTime;
	private Timestamp createTime;
}
