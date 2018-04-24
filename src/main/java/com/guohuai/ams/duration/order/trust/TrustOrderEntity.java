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

/**
 * 投资标的--申购订单表
 * @author star.zhu
 * 2016年5月17日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_TARGET_ORDER")
@DynamicInsert
@DynamicUpdate
public class TrustOrderEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 状态
	 */
	public static final String STATE_FAIL			= "-2";
	public static final String STATE_AUDIT			= "-1";
	public static final String STATE_APPOINTMENT	= "0";
	public static final String STATE_CONFIRM		= "1";
	public static final String STATE_SUCCESS 		= "2";
	
	/**
	 * 类型(purchase:申购;transfer:转入)
	 */
	public static final String TYPE_PURCHASE	= "purchase";
	public static final String TYPE_TRANSFER	= "transfer";
	
	public TrustOrderEntity() {
		this.applyVolume 	= BigDecimalUtil.init0;
		this.auditVolume 	= BigDecimalUtil.init0;
		this.reserveVolume 	= BigDecimalUtil.init0;
		this.investVolume 	= BigDecimalUtil.init0;
		this.applyCash 		= BigDecimalUtil.init0;
		this.auditCash 		= BigDecimalUtil.init0;
		this.reserveCash 	= BigDecimalUtil.init0;
		this.investCash 	= BigDecimalUtil.init0;
	}
	
	@Id
	private String oid;
	// 关联投资标的
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "targetOid", referencedColumnName = "oid")
	private Investment target;
	// 关联资产池
	private String assetPoolOid;
	// 投资日
	private Date investDate; 
	// 投资时间
	private Timestamp investTime;
	// 申购额度
	private BigDecimal applyVolume;
	// 审核额度
	private BigDecimal auditVolume;
	// 预约额度
	private BigDecimal reserveVolume;
	// 确认额度
	private BigDecimal investVolume;
	// 申购额度
	private BigDecimal applyCash;
	// 审核额度
	private BigDecimal auditCash;
	// 预约额度
	private BigDecimal reserveCash;
	// 确认额度
	private BigDecimal investCash;
	// 收益方式（amortized_cost：摊余成本法；book_value：账面价值法）
	private String profitType;
	// 申请人
	private String asker;
	// 审核人
	private String auditor; 
	// 预约人
	private String reserver; 
	// 状态（-2：失败，-1：待审核，0：待预约，1：待确认，2：已成立）
	private String state;
	// 类型(purchase:申购;transfer:转入)
	private String type;
	// 确认人
	private String confirmer; 
	// 确认时间
	private Date confirmDate;
	// 操作员
	private String operator;
	// UpdateTime
	private Timestamp updateTime;
	// CreateTime
	private Timestamp createTime;
}
