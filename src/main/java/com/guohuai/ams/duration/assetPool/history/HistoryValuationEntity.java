package com.guohuai.ams.duration.assetPool.history;

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
 * 资产池历史估值表
 * @author star.zhu
 * 2016年6月30日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_ESTIMATE")
@DynamicInsert
@DynamicUpdate
public class HistoryValuationEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String oid;
	// 关联资产池
	private String assetPoolOid;
	// 基准日
	private Date baseDate;
	// 估值
	private BigDecimal scale;
	// 净交易额
	private BigDecimal trade;
	// 费金
	private BigDecimal feeValue;
	
	private Timestamp createTime;
}
