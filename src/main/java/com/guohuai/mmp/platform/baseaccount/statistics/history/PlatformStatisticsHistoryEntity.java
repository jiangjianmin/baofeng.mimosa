package com.guohuai.mmp.platform.baseaccount.statistics.history;

import java.io.Serializable;
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
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-基本账户-统计-历史数据
 * 
 * @author wanglei
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_STATISTICS_HISTORY")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PlatformStatisticsHistoryEntity extends UUID implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4962497564579911579L;

	/**
	 * 所属平台
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "platformOid", referencedColumnName = "oid")
	PlatformBaseAccountEntity platformBaseAccount;

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
	 * 累计交易总额
	 */
	private BigDecimal totalTradeAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 发行人充值总额
	 */
	private BigDecimal publisherTotalDepositAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 发行人提现总额
	 */
	private BigDecimal publisherTotalWithdrawAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 投资人充值总额
	 */
	private BigDecimal investorTotalDepositAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 投资人提现总额
	 */
	private BigDecimal investorTotalWithdrawAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 注册投资人数
	 */
	private Integer registerAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 投资人数
	 */
	private Integer investorAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 持仓人数
	 */
	private Integer investorHoldAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 逾期次数
	 */
	private Integer overdueTimes = SysConstant.INTEGER_defaultValue;

	/**
	 * 发行产品数
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
	 * 在售产品数
	 */
	private Integer onSaleProductAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 发行人数
	 */
	private Integer publisherAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 实名认证人数
	 */
	private Integer verifiedInvestorAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 活跃投资人数
	 */
	private Integer activeInvestorAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 日期
	 */
	private Date confirmDate;

	private Timestamp updateTime;
	private Timestamp createTime;
}
