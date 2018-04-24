package com.guohuai.mmp.investor.baseaccount.statistics;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资人-基本账户-统计
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_STATISTICS")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorStatisticsEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = -2464621001909376882L;

	/**
	 * 所属投资人
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;

	/**
	 * 累计充值总额
	 */
	private BigDecimal totalDepositAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计提现总额
	 */
	private BigDecimal totalWithdrawAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计投资总额
	 */
	private BigDecimal totalInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计赎回总额
	 */
	private BigDecimal totalRedeemAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 累计收益总额
	 */
	private BigDecimal totalIncomeAmount = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 累计还本总额
	 */
	private BigDecimal totalRepayLoan = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 活期昨日收益额
	 */
	private BigDecimal t0YesterdayIncome = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 定期总收益额
	 */
	private BigDecimal tnTotalIncome = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 活期总收益额
	 */
	private BigDecimal t0TotalIncome = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 活期资产总额
	 */
	private BigDecimal t0CapitalAmount = SysConstant.BIGDECIMAL_defaultValue;
	
	/**定期总资产*/
	private BigDecimal tnCapitalAmount = SysConstant.BIGDECIMAL_defaultValue;
	
	/** 体验金总资产 */
	private BigDecimal experienceCouponAmount = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 累计投资产品总数量
	 */
	private Integer totalInvestProducts = SysConstant.INTEGER_defaultValue;
	
	
	

	

	/**
	 * 累计充值次数
	 */
	private Integer totalDepositCount = SysConstant.INTEGER_defaultValue;

	/**
	 * 累计提现次数
	 */
	private Integer totalWithdrawCount = SysConstant.INTEGER_defaultValue;

	/**
	 * 累计投资次数
	 */
	private Integer totalInvestCount = SysConstant.INTEGER_defaultValue;

	/**
	 * 累计赎回次数
	 */
	private Integer totalRedeemCount = SysConstant.INTEGER_defaultValue;

	/**
	 * 当日充值次数
	 */
	private Integer todayDepositCount = SysConstant.INTEGER_defaultValue;
	/**
	 * 当日提现次数
	 */
	private Integer todayWithdrawCount = SysConstant.INTEGER_defaultValue;
	/**
	 * 当日投资次数
	 */
	private Integer todayInvestCount = SysConstant.INTEGER_defaultValue;
	/**
	 * 当日赎回次数
	 */
	private Integer todayRedeemCount = SysConstant.INTEGER_defaultValue;
	
	/**
	 * 当日充值总额
	 */
	private Integer todayDepositAmount = SysConstant.INTEGER_defaultValue;
	
	/**
	 * 当日提现总额
	 */
	private Integer todayWithdrawAmount = SysConstant.INTEGER_defaultValue;
	
	
	/**
	 * 当日投资总额
	 */
	private Integer todayInvestAmount = SysConstant.INTEGER_defaultValue;
	
	
	/**
	 * 当日赎回总额
	 */
	private Integer todayRedeemAmount = SysConstant.INTEGER_defaultValue;
	
	
	/**
	 * 首次投资时间
	 */
	private Timestamp firstInvestTime;
	
	/**
	 * 收益确认日期
	 */
	private Date incomeConfirmDate;
	
	private Timestamp updateTime;
	
	private Timestamp createTime;
	
	
}
