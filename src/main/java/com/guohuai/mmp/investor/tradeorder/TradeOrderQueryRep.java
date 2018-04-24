package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TradeOrderQueryRep {

	/**
	 * 订单UUID
	 */
	private String tradeOrderOid;

	/**
	 * 产品UUID
	 */
	private String productOid;

	/**
	 * 产品名称
	 */
	String productName;

	/**
	 * 渠道UUID
	 */
	private String channelOid;

	/**
	 * 渠道名称
	 */
	private String channelName;
	
	/**
	 * 手机号
	 */
	private String phoneNum;

	/**
	 * 订单号
	 */
	private String orderCode;

	/**
	 * 订单类型
	 */
	private String orderType;
	private String orderTypeDisp;
	/**
	 * 针对奖励投资
	 */
	private String profitOtherDisp;

	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount;

	/**
	 * 订单份额
	 */
	private BigDecimal orderVolume;

	/**
	 * 订单状态
	 */
	private String orderStatus;
	private String orderStatusDisp;

	/**
	 * 合同生成状态
	 */
	private String contractStatus;
	private String contractStatusDisp;

	/**
	 * 订单创建人
	 */
	private String createMan;
	private String createManDisp;

	/**
	 * 订单时间
	 */
	private Timestamp orderTime;
	/**
	 * 订单完成时间
	 */
	private Timestamp completeTime;

	/**
	 * 发行人清算状态
	 */
	private String publisherClearStatus;
	private String publisherClearStatusDisp;

	/**
	 * 发行人交收状态
	 */
	private String publisherConfirmStatus;
	private String publisherConfirmStatusDisp;

	/***
	 * 发行人结算状态
	 */
	private String publisherCloseStatus;
	private String publisherCloseStatusDisp;

	/** 卡券类型 */
	private String couponType;
	private String couponTypeDisp;

	/** 卡券面值 */
	private BigDecimal couponAmount;

	private BigDecimal payAmount;

	/** 持有份额 */
	private BigDecimal holdVolume;
	/** 可赎回状态 */
	private String redeemStatus;
	private String redeemStatusDisp;
	/** 可计息状态 */
	private String accrualStatus;
	private String accrualStatusDisp;
	/** 开始起息日 */
	private Date beginAccuralDate;
	/** 开始赎回日 */
	private Date beginRedeemDate;
	/** 累计收益 */
	private BigDecimal totalIncome;
	/** 累计基础收益 */
	private BigDecimal totalBaseIncome;
	/** 累计奖励收益 */
	private BigDecimal totalRewardIncome;
	/** 昨日基础收益 */
	private BigDecimal yesterdayBaseIncome;
	/** 昨日奖励收益 */
	private BigDecimal yesterdayRewardIncome;
	/** 昨日收益 */
	private BigDecimal yesterdayIncome;
	/** 收益金额 */
	private BigDecimal incomeAmount;
	/** 预期收益Ext */
	private BigDecimal expectIncomeExt;
	/** 预期收益 */
	private BigDecimal expectIncome;
	/** 最新市值 */
	private BigDecimal value;
	/** 持有状态 */
	private String holdStatus;
	private String holdStatusDisp;
	
	/** 收益确认日期 */
	private Date confirmDate;

	private Timestamp createTime;

	private Timestamp updateTime;

	/**
	 * 基础收益率
	 */
	private BigDecimal baseIncomeRatio;
	/**
	 * 奖励收益率
	 */
	private BigDecimal rewardIncomeRatio;
	/**
	 * 奖励阶梯
	 */
	private String rewardIncomeLevel;
	/**
	 * 持仓天数
	 */
	private Long holdDays;
	/**
	 * 购买的渠道名称
	 */
	private String payChannelName;
	/**
	 * 是否预约扣款
	 */
	private String isAuto;
	
	/**
	 * 用户手机号
	 */
	private String userPhoneNum;
}
