package com.guohuai.ams.system.config.project.warrantyMode;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 担保方式权数配置
 * 
 * @author Arthur
 *
 */

@Entity
@Table(name = "T_GAM_CCP_WARRANTY_MODE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class CCPWarrantyMode implements Serializable {

	private static final long serialVersionUID = -3619709857554967150L;

	@Id
	private String oid;
	private String type;
	private String title;
	private BigDecimal weight;

}
