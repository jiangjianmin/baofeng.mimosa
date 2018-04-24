package com.guohuai.ams.cashtool;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class CashToolManageForm implements Serializable {

	private static final long serialVersionUID = -684099825649628579L;

	private String oid;
	/**
	 * 基金代码
	 */
	private String ticker;

	/**
	 * 基金名称
	 */
	private String secShortName;

	/**
	 * 基金类型
	 */
	private String etfLof;

	/**
	 * 投资对象
	 */
	private String investee;

	/**
	 * 运作模式
	 */
	private String operationMode;

	/**
	 * 是否QDII
	 */
	private String isQdii;

	/**
	 * 是否isFof
	 */
	private String isFof;

	/**
	 * 是否保本
	 */
	private String isGuarFund;

	/**
	 * 保本周期（月）
	 */
	private BigDecimal guarPeriod;

	/**
	 * 保本比例
	 */
	private BigDecimal guarRatio;

	/**
	 * 交易所代码
	 */
	private String exchangeCd;

	/**
	 * 上市状态
	 */
	private String listStatusCd;

	/**
	 * 成立日期
	 */
	private Date establishDate;

	/**
	 * 上市日期
	 */
	private Date listDate;

	/**
	 * 终止上市日期
	 */
	private Date delistDate;

	/**
	 * 基金管理人编码
	 */
	private String managementCompany;

	/**
	 * 基金管理人名称
	 */
	private String managementFullName;

	/**
	 * 基金托管人编码
	 */
	private String custodian;

	/**
	 * 基金托管人名称
	 */
	private String custodianFullName;

	/**
	 * 投资领域
	 */
	private String investField;

	/**
	 * 投资目标
	 */
	private String investTarget;

	/**
	 * 业绩比较基准
	 */
	private String perfBenchmark;

	/**
	 * 最新流通份额
	 */
	private BigDecimal circulationShares;

	/**
	 * 是否分级基金
	 */
	private String isClass;

	/**
	 * 交易简称
	 */
	private String tradeAbbrName;

	/**
	 * 基金经理
	 */
	private String managerName;

	/**
	 * 万份收益日（日期）
	 */
	private Date dailyProfitDate;

	/**
	 * 万份收益
	 */
	private BigDecimal dailyProfit;

	/**
	 * 7日年化收益率
	 */
	private BigDecimal weeklyYield;

	/**
	 * 持有份额
	 */
	private BigDecimal holdAmount;

	/**
	 * 风险等级
	 */
	private String riskLevel;

	/**
	 * 分红方式
	 */
	private String dividendType;

	/**
	 * 资产净值
	 */
	private BigDecimal netAsset;

	/**
	 * 基金管理费
	 */
	private BigDecimal charge;

	/**
	 * 基金托管费
	 */
	private BigDecimal custody;

	/**
	 * 基金公告
	 */
	private String report;

	/**
	 * 状态
	 */
	private String state;
	
	/**
	 * 申赎确认日
	 */
	@NotNull(message = "申赎确认日不能为空")
	private Integer confirmDays;
	
	/**
	 * 收益结转方式
	 */
	@NotNull(message = "收益结转方式不能为空")
	@Size(max = 32, message = "收益结转方式最长32个字符！")
	private String incomeSchedule;
	
	/**
	 * 持有目的
	 */
	@NotNull(message = "持有目的不能为空")
	@Size(max = 32, message = "持有目的最长32个字符！")
	private String holdPorpush;

	/**
	 * 创建员
	 */
	private String creator;

	/**
	 * 操作员
	 */
	private String operator;

	/**
	 * 创建时间
	 */
	private Timestamp createTime;

	/**
	 * 修改时间
	 */
	private Timestamp updateTime;
}
