package com.guohuai.ams.system.config.risk.indicate;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.system.config.risk.cate.RiskCate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风险指标库
 * 
 * @author Arthur
 *
 */

@Entity
@Table(name = "T_GAM_CCR_RISK_INDICATE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class RiskIndicate implements Serializable {

	private static final long serialVersionUID = -7174518448416295162L;

	public static final String STATE_Enable = "ENABLE";
	public static final String STATE_Disable = "DISABLE";
	public static final String STATE_Delete = "DELETE";

	public static final String DATA_TYPE_Number = "NUMBER";
	public static final String DATA_TYPE_NumRange = "NUMRANGE";
	public static final String DATA_TYPE_Text = "TEXT";

	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cateOid", referencedColumnName = "oid")
	private RiskCate cate;
	private String title;
	private String state;
	private String dataType;
	private String dataUnit;

}
