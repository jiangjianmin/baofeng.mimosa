package com.guohuai.mmp.publisher.baseaccount;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PublisherBaseAccountRep extends BaseRep {
	
	/**
	 * 发行人OID
	 */
	private String publisherOid;
	/**
	 * 账户余额
	 */
	private BigDecimal accountBalance = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 累计充值总额
	 */
	private BigDecimal totalDepositAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计提现总额
	 */
	private BigDecimal totalWithdrawAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计借款总额
	 */
	private BigDecimal totalLoanAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计还款总额
	 */
	private BigDecimal totalReturnAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计付息总额
	 */
	private BigDecimal totalInterestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 今日投资总额
	 */
	private BigDecimal todayInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 今日活期投资额
	 */
	private BigDecimal todayT0InvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 今日定期投资总额
	 */
	private BigDecimal todayTnInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 今日赎回金额
	 */
	private BigDecimal todayRedeemAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 今日还本金额
	 */
	private BigDecimal todayRepayInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 今日付息金额
	 */
	private BigDecimal todayRepayInterestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 逾期次数
	 */
	private Integer overdueTimes = SysConstant.INTEGER_defaultValue;

	/**
	 * 发行产品总数
	 */
	private Integer productAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 已结算产品数
	 */
	private Integer closedProductAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 待结算产品数
	 */
	private Integer toCloseProductAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 总投资人数
	 */
	private Integer investorAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 现持仓人数
	 */
	private Integer investorHoldAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 今日活期投资人数
	 */
	private Integer todayT0InvestorAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 今日定期投资人数
	 */
	private Integer todayTnInvestorAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 今日投资人数
	 */
	private Integer todayInvestorAmount = SysConstant.INTEGER_defaultValue;

}
