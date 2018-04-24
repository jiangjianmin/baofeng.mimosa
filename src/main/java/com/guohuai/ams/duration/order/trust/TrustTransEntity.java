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

import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

/**
 * 投资标的--资产转让订单
 * @author star.zhu
 * 2016年5月17日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_TARGET_TRANS")
@DynamicInsert
@DynamicUpdate
public class TrustTransEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public TrustTransEntity() {
		this.tranCash 		= BigDecimalUtil.init0;
		this.tranVolume 	= BigDecimalUtil.init0;
		this.auditVolume 	= BigDecimalUtil.init0;
		this.investVolume 	= BigDecimalUtil.init0;
		this.auditCash 		= BigDecimalUtil.init0;
		this.investCash 	= BigDecimalUtil.init0;
	}

	@Id
	private String oid;
	// 关联资产池投资标的
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "targetOid", referencedColumnName = "oid")
	private TrustEntity trustEntity;
	// 转让份额
	private BigDecimal tranVolume;
	// 审核份度
	private BigDecimal auditVolume;
	// 确认份度
	private BigDecimal investVolume;
	// 审核金额
	private BigDecimal auditCash;
	// 确认金额
	private BigDecimal investCash;
	// 转让日期
	private Date tranDate; 
	// 转让时间
	private Timestamp tranTime;
	// 转让溢价
	private BigDecimal tranCash; 
	// 转让操作员
	private String creater;
	// 状态（-2：失败，-1：待审核，0：待预约，1：待确认，2：已成立）
	private String state;
	// 申请人
	private String asker;
	// 审核人
	private String auditor; 
	// 确认人
	private String confirmer; 
	// 确认时间
	private Date confirmDate;
	// 操作员
	private String operator;
	// 逾期转让
	private boolean overdueFlag;
	// 转让类型
	private String type;
	// 受让方名称
	private String transferee;
	// UpdateTime
	private Timestamp updateTime;
	// CreateTime
	private Timestamp createTime;
}
