package com.guohuai.ams.duration.assetPool;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.Data;

/**
 * 存续期--资产池对象
 * @author star.zhu
 * 2016年5月16日
 */
@Data
public class AssetPoolForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String name;
	// 真实市值
	private BigDecimal marketValue;
	// 资产规模
	private BigDecimal scale;
	// 投资范围
	private String[] scopes;
	// 现金比例
	private BigDecimal cashRate;
	// 货币基金（现金类管理工具）比例
	private BigDecimal cashtoolRate;
	// 信托（计划）比例
	private BigDecimal targetRate;
	// 现金实际比例
	private BigDecimal cashFactRate;
	// 货币基金（现金类管理工具）实际比例
	private BigDecimal cashtoolFactRate;
	// 信托（计划）实际比例
	private BigDecimal targetFactRate;
	// 可用现金
	private BigDecimal cashPosition;
	// 冻结资金
	private BigDecimal freezeCash;
	// 在途资金
	private BigDecimal transitCash;
	// 确认收益
	private BigDecimal confirmProfit;
	// 实现收益
	private BigDecimal factProfit;
	// 偏离损益
	private BigDecimal deviationValue;
	// 每日定时任务状态(未计算，已计算，部分计算，当日不计算)
	private String scheduleState;
	// 当日收益分配状态(未分配，已分配)
	private String incomeState;
	// 是否录入真实估值(0:否;1:是)
	private int factValuation;
	// 费用计算基础
	private int calcBasis;
	// 未分配收益
	private BigDecimal unDistributeProfit;
	// 应付费金
	private BigDecimal payFeigin;
	// spv所有者权益
	private BigDecimal spvProfit;
	// 投资者所有权益
	private BigDecimal investorProfit;
	// 状态
	private String state;
	// 创建者
	private String creater;
	// 操作员
	private String operator;
	// 估值基准日
	private Date baseDate;
	// 今日实际份额
	private BigDecimal shares;
	// 今日单位净值
	private BigDecimal nav;
	// SPVOid
	private String spvOid;
	// SPVName
	private String spvName;
	//  SPV累计计提费金
	private BigDecimal countintChargefee;
	// SPV累计提取费金
	private BigDecimal drawedChargefee;
	// 资管机构名称
	private String organization;
	// 资管计划名
	private String planName;
	// 银行名称
	private String bank;
	// 银行账号
	private String account;
	// 联系人
	private String contact;
	// 联系电话
	private String telephone;
	// 基础资产编号
	private String baseAssetCode;
	// 创建日期
	private Timestamp createTime;
	// 更新日期
	private Timestamp updateTime;
	// 资管计提费用算法

	
	// 资产净值
	private BigDecimal netValue;
	// 托管费率
	private BigDecimal trusteeRate;
	// 应付托管费
	private BigDecimal trusteeFee;
	// 管理费率
	private BigDecimal manageRate;
	// 应付管理费
	private BigDecimal manageFee;
	// 单人累计申购上限
	private BigDecimal purchaseLimit;
}
