package com.guohuai.ams.duration.fact.calibration;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.order.SPVOrder;

import lombok.Data;

/**
 * 市值校准关联订单
 * 
 * @author star.zhu
 *         2016年6月16日
 */
@Entity
@Data
@Table(name = "T_GAM_ASSETPOOL_MARKET_ADJUST_ORDER")
@DynamicInsert
@DynamicUpdate
public class MarketAdjustOrderEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String oid;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "adjustOid", referencedColumnName = "oid")
	private MarketAdjustEntity adjust;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "orderOid", referencedColumnName = "oid")
	private SPVOrder order;
}
