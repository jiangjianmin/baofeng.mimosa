package com.guohuai.ams.system.config.risk.warning.options;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.warning.RiskWarning;
import com.guohuai.component.persist.UUID;
import com.guohuai.component.util.StringUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 预警指标配置表
 * <p>
 * Title: RiskWarningOptions.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年6月7日 下午4:31:53
 */
@Entity
@Table(name = "T_GAM_CCR_RISK_WARNING_OPTIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@DynamicInsert
@DynamicUpdate
public class RiskWarningOptions extends UUID {

	private static final long serialVersionUID = 8777749354109943921L;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "wariningOid", referencedColumnName = "oid")
	private RiskWarning warning;
	private String wlevel;
	private String param0;
	private String param1;
	private String param2;
	private String param3;
	private Integer seq;

	public String showTitle() {
		RiskIndicate indicate = this.warning.getIndicate();
		String dataUnit = indicate.getDataUnit();
		if (null == this.warning)
			return null;
		// if (this.dft.equals("YES")) {
		// return "N/A";
		// }
		if (indicate.getDataType().equals(RiskIndicate.DATA_TYPE_Number)) {
			return formatTitle(this.param0, dataUnit);
		}
		if (indicate.getDataType().equals(RiskIndicate.DATA_TYPE_NumRange)) {
			Object[] param = new Object[4];
			param[0] = this.param0;
			param[1] = formatTitle(StringUtil.isEmpty(this.param1) ? "∞" : this.param1, dataUnit);
			param[2] = formatTitle(StringUtil.isEmpty(this.param2) ? "∞" : this.param2, dataUnit);
			param[3] = this.param3;
			return String.format("%s %s, %s %s", param);
		}
		if (indicate.getDataType().equals(RiskIndicate.DATA_TYPE_Text)) {
			return this.param0;
		}
		return null;
	}

	private static String formatTitle(String title, String dataUnit) {
		if (StringUtils.isBlank(title) || title.equals("∞"))
			return title;
		return title + (dataUnit == null ? "" : dataUnit.trim());
	}

}
