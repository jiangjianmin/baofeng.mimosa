package com.guohuai.ams.system.config.risk.warning;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskWarningForm {
	String oid;
	@NotBlank
	@NotEmpty
	@NotNull
	private String indicateOid;

	@NotNull
	@Length(max = 120, message = "指标项描述长度不能超过120（包含）！")
	String title;

}
