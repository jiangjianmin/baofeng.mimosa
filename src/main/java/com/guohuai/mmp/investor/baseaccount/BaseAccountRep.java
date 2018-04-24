package com.guohuai.mmp.investor.baseaccount;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BaseAccountRep extends BaseRep {
	
	/**
	 * 用户OID
	 */
	private String userOid;
	
	/**
	 * 业务系统用户OID
	 */
	private String memberId;

	/**
	 * 全手机号
	 */
	private String phone;
	
	/**
	 * 手机号
	 */
	private String phoneNum;

	/**
	 * 真实姓名
	 */
	private String realName;

	/**
	 * 银行名称
	 */
	private String bankName;
	
	/**
	 * 银行卡号
	 */
	private String cardNum;
	
	/**
	 * 状态
	 */
	private String status;
	private String statusDisp;

	/**
	 * 余额
	 */
	private BigDecimal balance;

	/**
	 * 账户所有者
	 */
	private String owner;
	private String ownerDisp;
	
	/**
	 * 是否新手
	 */
	private String isFreshman;
	private String isFreshmanDisp;
	
	
	/**
	 * 累计充值总额
	 */
	private BigDecimal totalDepositAmount;

	/**
	 * 累计提现总额
	 */
	private BigDecimal totalWithdrawAmount;

	/**
	 * 累计投资总额
	 */
	private BigDecimal totalInvestAmount;

	/**
	 * 累计赎回总额
	 */
	private BigDecimal totalRedeemAmount;

	/**
	 * 累计收益总额
	 */
	private BigDecimal totalIncomeAmount;
	
	/**
	 * 累计还本总额
	 */
	private BigDecimal totalRepayLoan;

	/**
	 * 活期昨日收益额
	 */
	private BigDecimal t0YesterdayIncome;

	/**
	 * 定期总收益额
	 */
	private BigDecimal tnTotalIncome;

	/**
	 * 活期总收益额
	 */
	private BigDecimal t0TotalIncome;

	/**
	 * 活期资产总额
	 */
	private BigDecimal t0CapitalAmount;
	
	/**定期总资产*/
	private BigDecimal tnCapitalAmount;
	
	
	/**
	 * 累计投资产品总数量
	 */
	private Integer totalInvestProducts;

	/**
	 * 累计充值次数
	 */
	private Integer totalDepositCount;

	/**
	 * 累计提现次数
	 */
	private Integer totalWithdrawCount;

	/**
	 * 累计投资次数
	 */
	private Integer totalInvestCount;

	/**
	 * 累计赎回次数
	 */
	private Integer totalRedeemCount;

	/**
	 * 当日充值次数
	 */
	private Integer todayDepositCount;
	/**
	 * 当日提现次数
	 */
	private Integer todayWithdrawCount;
	/**
	 * 当日投资次数
	 */
	private Integer todayInvestCount;
	/**
	 * 当日赎回次数
	 */
	private Integer todayRedeemCount;
	
	/**
	 * 当日充值总额
	 */
	private Integer todayDepositAmount;
	/**
	 * 当日提现总额
	 */
	private Integer todayWithdrawAmount;
	/**
	 * 当日投资总额
	 */
	private Integer todayInvestAmount;
	/**
	 * 当日赎回总额
	 */
	private Integer todayRedeemAmount;
	
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
	
	/**
	 * 基金总额
	 */
	private BigDecimal fundAmount;
}
