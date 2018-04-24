package com.guohuai.ams.duration.capital.calc.fund;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.capital.calc.AssetPoolCalc;
import com.guohuai.ams.duration.order.fund.FundEntity;
import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

/**
 * 现金管理工具每日收益表
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_CASHTOOL_INCOME_DETAIL")
@DynamicInsert
@DynamicUpdate
public class FundCalc implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public FundCalc() {
		this.capital 	= BigDecimalUtil.init0;
		this.yield 		= BigDecimalUtil.init0;
		this.interest 	= BigDecimalUtil.init0;
		this.income 	= BigDecimalUtil.init0;
	}
	
	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cashtoolOid", referencedColumnName = "oid")
	private FundEntity fundEntity;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assetPoolIncomeOid", referencedColumnName = "oid")
	private AssetPoolCalc assetPoolCalc;
	// 本金
	private BigDecimal capital;
	// 复利本金
	private BigDecimal interest;
	// 收益率
	private BigDecimal yield;
	// 收益
	private BigDecimal income;
	// 收益基准日
	private Date baseDate;
	// 创建日期
	private Timestamp createTime;
	// 更新日期
	private Timestamp updateTime;
	
}
