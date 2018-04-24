package com.guohuai.ams.duration.order.trust;

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

import com.guohuai.ams.investment.Investment;
import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_TARGET")
@DynamicInsert
@DynamicUpdate
public class TrustEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 标的类型-信托标的
	 */
	public static final String TRUST = "target";
	
	/**
	 * 状态
	 */
	public static final String INVESTING = "0";		// 持仓中
	public static final String CANCEL = "1";		// 已坏账核销，待确认
	public static final String INVESTEND = "-1";	// 已清仓
	
	public TrustEntity() {
		this.confirmVolume 	= BigDecimalUtil.init0;
		this.investVolume 	= BigDecimalUtil.init0;
		this.interestAcount = BigDecimalUtil.init0;
		this.purchaseAcount = BigDecimalUtil.init0;
		this.redeemAcount 	= BigDecimalUtil.init0;
		this.transOutVolume = BigDecimalUtil.init0;
		this.transOutFee 	= BigDecimalUtil.init0;
		this.transInVolume 	= BigDecimalUtil.init0;
		this.transInFee 	= BigDecimalUtil.init0;
		this.dailyProfit 	= BigDecimalUtil.init0;
		this.totalProfit 	= BigDecimalUtil.init0;
	}

	@Id
	private String oid;
	// 关联投资标的
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "targetOid", referencedColumnName = "oid")
	private Investment target;
	// 关联订单
	private String orderOid;
	// 关联产品
	private String assetPoolOid;
	// 收益分配方式
	private String returnsType;
	// 申购类型(PURCHASE;TRANS_IN)
	private String purchase;
	// 投资日
	private Date investDate; 
	// 状态(0:投资成功;-1:投资结束;1:坏账核销)
	private String state;
	// 申请份额
	private BigDecimal applyVolume;
	// 申请金额
	private BigDecimal applyCash;
	// 批准份额
	private BigDecimal confirmVolume;
	// 投资(持有)份额
	private BigDecimal investVolume;
	// 起息份额
	private BigDecimal interestAcount;
	// 申购当日不计息份额
	private BigDecimal purchaseAcount;
	// 赎回当日计息份额
	private BigDecimal redeemAcount;
	// 起息日
	private Date interestDate;
	// 转出份额
	private BigDecimal transOutVolume;
	// 转出费用
	private BigDecimal transOutFee;
	// 转入份额
	private BigDecimal transInVolume;
	// 转入费用
	private BigDecimal transInFee;
	// 收益方式（amortized_cost：摊余成本法；book_value：账面价值法）
	private String profitType;
	// 每日收益
	private BigDecimal dailyProfit;
	// 累计收益
	private BigDecimal totalProfit;
	
	private Timestamp createTime;
	private Timestamp updateTime;
}
