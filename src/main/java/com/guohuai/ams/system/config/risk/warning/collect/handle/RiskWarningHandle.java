package com.guohuai.ams.system.config.risk.warning.collect.handle;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.system.config.risk.warning.collect.RiskWarningCollect;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_GAM_CCR_RISK_WARNING_HANDLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@DynamicInsert
@DynamicUpdate
public class RiskWarningHandle extends UUID implements Serializable {

	/**
	 * 处置结果
	 */
	public static final String HANDLE_KEEPLEVEL = "KEEP"; // 保留风险等级
	public static final String HANDLE_CLEARLEVEL = "CLEAR"; // 风险已处置
	public static final String HANDLE_DOWNLEVEL = "DOWN"; // 风险降级

	private static final long serialVersionUID = -1892346176260766504L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collectOid", referencedColumnName = "oid")
	private RiskWarningCollect riskWarningCollect; // 预警数据
	private String report; // 处置报告
	private String meeting; // 过会纪要
	private String summary; // 处置摘要
	private String handle; // 处置结果
	private Timestamp createTime; // 处置时间
}
