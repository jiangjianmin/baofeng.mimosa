/*   
 * Copyright © 2015 guohuaigroup All Rights Reserved.   
 *   
 * This software is the confidential and proprietary information of   
 * Founder. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Founder.   
 *   
 */
package com.guohuai.ams.cashtool;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @ClassName: CashTool
 * @Description: 现金管理工具实体
 * @author vania
 * @date 2016年5月16日 上午10:15:56
 *
 */
@Entity
@Table(name = "T_GAM_CASHTOOL")
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class CashTool extends UUID {

	private static final long serialVersionUID = -3696995771339594216L;
	
	/**
	 * 现金管理类工具状态
	 * waitPretrial 未审核
	 * pretrial 审核中
	 * checkpass 审核通过
	 * reject 驳回
	 * invalid 作废
	 * delete删除
	 */
	public static final String CASHTOOL_STATE_waitPretrial = "waitPretrial";
	public static final String CASHTOOL_STATE_pretrial = "pretrial";
	public static final String CASHTOOL_STATE_collecting = "collecting";
	public static final String CASHTOOL_STATE_reject = "reject";
	public static final String CASHTOOL_STATE_invalid = "invalid";
//	public static final String CASHTOOL_STATE_delete = "delete";

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
	private Integer confirmDays;
	
	/**
	 * 收益结转方式
	 */
	private String incomeSchedule;
	/**
	 * 持有目的
	 */
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

	public CashTool(String oid, String secShortName) {
		this.oid = oid;
		this.secShortName = secShortName;
	}
}
