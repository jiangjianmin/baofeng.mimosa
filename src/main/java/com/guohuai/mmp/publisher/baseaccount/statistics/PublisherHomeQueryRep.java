package com.guohuai.mmp.publisher.baseaccount.statistics;

import java.math.BigDecimal;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人首页查询
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherHomeQueryRep extends BaseRep {

	/** 累计借款总额 */
	private BigDecimal totalLoanAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 累计还款总额 */
	private BigDecimal totalReturnAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 账户余额 */
	private BigDecimal balance = SysConstant.BIGDECIMAL_defaultValue;

	/** 借款账号总数（总投资人数） */
	private Integer investorCount = SysConstant.INTEGER_defaultValue;

	/** 现借款账号总数（现持仓人数） */
	private Integer investorHoldCount = SysConstant.INTEGER_defaultValue;

	/** 现借金额 */
	private BigDecimal loanAmountNow = SysConstant.BIGDECIMAL_defaultValue;

	/** 累计充值总额 */
	private BigDecimal totalDepositAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 累计提现总额 */
	private BigDecimal totalWithdrawAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 昨日产品投资额Top5 */
	private List<PublisherTop5ProRep> top5ProductList;

	/** 在售产品募集进度 */
	private List<PublisherRaiseRateRep> raiseRate;

	/** 投资人质量分析 */
	private List<PublisherInvestorAnalyseRep> investorAnalyse;
}
