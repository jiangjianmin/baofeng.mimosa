package com.guohuai.ams.system.config.risk.warning.collect;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.system.config.risk.warning.RiskWarning;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 预警数据采集
 * 
 * @author lirong
 *
 */
@Entity
@Table(name = "T_GAM_CCR_RISK_WARNING_COLLECT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@DynamicInsert
@DynamicUpdate
public class RiskWarningCollect extends UUID implements Serializable {

	private static final long serialVersionUID = -2819061270010548090L;
	
	/**
	 * 风险预警等级
	 */
	public static final String COLLECT_LEVEL_HIGH = "HIGH"; //高
	public static final String COLLECT_LEVEL_MID = "MID"; //中
	public static final String COLLECT_LEVEL_LOW = "LOW"; //低
	public static final String COLLECT_LEVEL_NONE = "NONE"; //无
	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wariningOid", referencedColumnName = "oid")
	private RiskWarning riskWarning; // 预警指标
	private String relative; // 关联键
	private String collectOption; // 采集选项
	private String collectData; // 采集数据
	private String wlevel; // 风险等级
	private String handleLevel; // 处置后风险等级
	private Timestamp createTime; // 创建时间

}
