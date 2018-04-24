package com.guohuai.ams.duration.order.fund;

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

import com.guohuai.ams.cashtool.CashTool;
import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_CASHTOOL")
@DynamicInsert
@DynamicUpdate
public class FundEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 标的类型-现金类管理工具
	 */
	public static final String FUND = "cashTool";

	/**
	 * 状态
	 */
	public static final String INVESTING = "0";
	public static final String INVESTEND = "-1";
	
	public FundEntity() {
		this.amount 		= BigDecimalUtil.init0;
		this.interestAcount = BigDecimalUtil.init0;
		this.purchaseAcount = BigDecimalUtil.init0;
		this.redeemAcount 	= BigDecimalUtil.init0;
		this.purchaseVolume = BigDecimalUtil.init0;
		this.redeemVolume 	= BigDecimalUtil.init0;
		this.frozenCapital 	= BigDecimalUtil.init0;
		this.onWayCapital 	= BigDecimalUtil.init0;
		this.dailyProfit 	= BigDecimalUtil.init0;
		this.totalProfit 	= BigDecimalUtil.init0;
	}

	@Id
	private String oid;
	// 关联现金管理工具
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cashtoolOid", referencedColumnName = "oid")
	private CashTool cashTool;
//	private String cashtoolOid;
	// 关联资产池
	private String assetPoolOid;
	// 持有份额
	private BigDecimal amount;
	// 起息份额
	private BigDecimal interestAcount;
	// 申购当日不计息份额
	private BigDecimal purchaseAcount;
	// 赎回当日计息份额
	private BigDecimal redeemAcount;
	// 起息日
	private Date interestDate;
	// 申购中份额
	private BigDecimal purchaseVolume;
	// 赎回中份额
	private BigDecimal redeemVolume;
	// 冻结资金
	private BigDecimal frozenCapital;
	// 在途资金
	private BigDecimal onWayCapital;
	// 每日收益
	private BigDecimal dailyProfit;
	// 累计收益
	private BigDecimal totalProfit;
	// 状态(0：生效 ;-1：未生效 )
	private String state;
	
	private Timestamp createTime;
	private Timestamp updateTime;
}
