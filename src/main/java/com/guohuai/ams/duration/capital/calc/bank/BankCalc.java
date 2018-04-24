package com.guohuai.ams.duration.capital.calc.bank;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

@Data
@Entity
@Table(name = "T_GAM_CALCULATE_BANK")
@DynamicInsert
@DynamicUpdate
public class BankCalc implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public BankCalc() {
		this.capital 		= BigDecimalUtil.init0;
		this.yield 			= BigDecimalUtil.init0;
		this.profit 		= BigDecimalUtil.init0;
		this.dailyProfit 	= BigDecimalUtil.init0;
		this.totalProfit 	= BigDecimalUtil.init0;
		this.factProfit 	= BigDecimalUtil.init0;
	}
	
	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pid", referencedColumnName = "oid")
	private AssetPoolEntity assetPool;
	// 本金
	private BigDecimal capital;
	// 收益率
	private BigDecimal yield;
	// 收益
	private BigDecimal profit;
	// 每日收益
	private BigDecimal dailyProfit;
	// 总收益
	private BigDecimal totalProfit;
	// 实际收益
	private BigDecimal factProfit;
	// 创建日期
	private Timestamp createTime;
	// 更新日期
	private Timestamp updateTime;
	// 操作人
	private String operator;
}
