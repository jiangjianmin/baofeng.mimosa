package com.guohuai.ams.system.config.risk.warning.options;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;

import com.guohuai.ams.system.config.risk.cate.RiskCate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.warning.RiskWarning;
import com.guohuai.component.web.parameter.validation.Enumerations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskWarningOptionsForm {
	@NotNull
	@NotEmpty
	@NotBlank
	private String cateOid;
	private String cateTitle;

	@NotNull
	@NotEmpty
	@NotBlank
	private String indicateOid;
	private String indicateTitle;
	private String indicateDataType;
	private String indicateDataUnit;

	private String warningOid;
	@NotNull
	@NotEmpty
	@NotBlank
	private String warningTitle;
	@NotNull
	@Size(max = 10)
	private List<Option> options;

	@Data
	public static class Option {

		@NotNull
		@NotEmpty
		@NotBlank
		@Enumerations(values = { "", "NONE", "LOW", "MID", "HIGH" })
		private String wlevel;
		private String param0;
		private String param1;
		private String param2;
		private String param3;

	}

	public RiskWarningOptionsForm(List<RiskWarningOptions> list) {
		int size = null == list ? 0 : list.size();
		if (size == 0)
			return;
		RiskWarning warning = list.get(0).getWarning();
		RiskIndicate indicate = warning.getIndicate();
		RiskCate cate = indicate.getCate();

		this.cateOid = cate.getOid();
		this.cateTitle = cate.getTitle();

		this.indicateOid = indicate.getOid();
		this.indicateTitle = indicate.getTitle();
		this.indicateDataType = indicate.getDataType();
		this.indicateDataUnit = indicate.getDataUnit();

		this.warningOid = warning.getOid();
		this.warningTitle = warning.getTitle();

		// this.oid = options.getOid();

		List<Option> options = new ArrayList<>(size);
		for (RiskWarningOptions riskWarningOptions : list) {
			Option op = new Option();
			BeanUtils.copyProperties(riskWarningOptions, op);
			options.add(op);
		}
		this.options = options;
	}
}
