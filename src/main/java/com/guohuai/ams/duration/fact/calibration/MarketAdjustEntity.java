package com.guohuai.ams.duration.fact.calibration;

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

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;

import lombok.Data;

/**
 * 市值校准
 * 
 * @author star.zhu
 *         2016年6月16日
 */
@Entity
@Data
@Table(name = "T_GAM_ASSETPOOL_MARKET_ADJUST")
@DynamicInsert
@DynamicUpdate
public class MarketAdjustEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 状态 (待审核: CREATE;通过: PASS;驳回: FAIL;已删除: DELETE)
	 */
	public static final String INIT = "init";
	public static final String CREATE = "create";
	public static final String PASS = "pass";
	public static final String FAIL = "fail";
	public static final String DELETE = "delete";

	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "assetpoolOid", referencedColumnName = "oid")
	private AssetPoolEntity assetPool;
	// 上一个校准日
	private Date lastBaseDate;
	// 校准日
	private Date baseDate;
	// 份额
	private BigDecimal shares;
	// 单位净值
	private BigDecimal nav;
	// 净申购
	private BigDecimal purchase;
	// 净赎回
	private BigDecimal redemption;
	// 净收益
	private BigDecimal profit;
	// 昨日净申赎
	private BigDecimal lastOrders;
	// 昨日份额
	private BigDecimal lastShares;
	// 昨日单位净值
	private BigDecimal lastNav;
	// 收益率
	private BigDecimal ratio;
	// 申请人
	private String creator;
	// 申请时间
	private Timestamp createTime;
	// 审批人
	private String auditor;
	// 审批时间
	private Timestamp auditTime;
	// 状态 (待审核: CREATE;通过: PASS;驳回: FAIL;已删除: DELETE)
	private String status;

	private String aversion;
}
