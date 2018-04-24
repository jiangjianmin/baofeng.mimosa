package com.guohuai.ams.system.config.risk.options;

import java.util.List;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class RiskOptionsForm {
	@NotNull
	@NotEmpty
	@NotBlank
	private String cateOid;
	private String cateTitle;

	private String indicateOid;
	private String indicateTitle;
	private String indicateDataType;
	private String indicateDataUnit;
	@Digits(integer = 4, fraction = 0)
	private int dftScore;
	private List<Option> options;

	@Data
	public static class Option {

		@NotNull
		@Digits(integer = 4, fraction = 0)
		private Integer score;
		@Length(max = 60, message = "指标项描述长度不能超过60（包含）！")
		private String param0;
		private String param1;
		private String param2;
		private String param3;

	}

}
