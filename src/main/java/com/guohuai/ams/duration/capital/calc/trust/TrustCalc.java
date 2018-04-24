package com.guohuai.ams.duration.capital.calc.trust;

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
import com.guohuai.ams.duration.order.trust.TrustEntity;
import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_TARGET_INCOME_DETAIL")
@DynamicInsert
@DynamicUpdate
public class TrustCalc implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public TrustCalc() {
		this.capital 	= BigDecimalUtil.init0;
		this.yield 		= BigDecimalUtil.init0;
		this.profit 	= BigDecimalUtil.init0;
	}
	
	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "targetOid", referencedColumnName = "oid")
	private TrustEntity trustEntity;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assetPoolIncomeOid", referencedColumnName = "oid")
	private AssetPoolCalc assetPool;
	// 本金
	private BigDecimal capital;
	// 收益率
	private BigDecimal yield;
	// 收益
	private BigDecimal profit;
	// 收益基准日
	private Date baseDate;
	// 创建日期
	private Timestamp createTime;
	// 更新日期
	private Timestamp updateTime;
	// 类型(募集期；存续期；)
	private String type;
}
