package com.guohuai.ams.duration.fact.calibration;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

/**
 * 估值校准记录
 * @author star.zhu
 * 2016年6月29日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_ESTIMATE_ADJUST_LOG")
@DynamicInsert
@DynamicUpdate
public class TargetAdjustEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String oid;
	// 关联资产池
	private String assetPoolOid;
	// 类型
	private String type;
	// 关联现金管理工具(持仓)
	private String cashtoolOid;
	// 关联投资标的(持仓)
	private String targetOid;
	// 原始份额
	private BigDecimal baseAmount;
	// 调整份额
	private BigDecimal newAmount;
	// 变动份额
	private BigDecimal changeAmount;
	private Timestamp createTime;
	private Date operaterDate;
	
}
