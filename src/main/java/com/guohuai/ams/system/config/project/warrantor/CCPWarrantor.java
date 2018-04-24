package com.guohuai.ams.system.config.project.warrantor;

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
 * 担保对象权数配置
 * 
 * @author Arthur
 *
 */

@Entity
@Table(name = "T_GAM_CCP_WARRANTOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class CCPWarrantor implements Serializable {

	private static final long serialVersionUID = -1196776787579317636L;

	@Id
	private String oid;
	private String title;
	private int lowScore;
	private int highScore;
	private BigDecimal weight;

}
