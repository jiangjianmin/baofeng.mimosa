package com.guohuai.ams.system.config.project.warrantyLevel;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 风险等级配置
 * 
 * @author vania
 *
 */

@Entity
@Table(name = "T_GAM_CCP_WARRANTY_LEVEL")
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class CCPWarrantyLevel extends UUID {

	private static final long serialVersionUID = -7004355457279663225L;

	private String wlevel;
	private String name;
	private String coverLow;
	private BigDecimal lowFactor;
	private BigDecimal highFactor;
	private String coverHigh;

}
