package com.guohuai.ams.system.config.risk.indicate;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import com.guohuai.component.web.parameter.validation.Enumerations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskIndicateForm {

	private String cateOid;
	
	@Enumerations(values = { "WARNING", "SCORE" })
	private String cateType;
	
	@Length(max = 30, message = "指标项名称长度不能超过30（包含）！")
	private String cateTitle;
	
	private String indicateOid;
	
	@NotBlank
	@NotEmpty
	@NotNull
	@Length(max = 120, message = "指标项名称长度不能超过120（包含）！")
	private String indicateTitle;
	
	@Enumerations(values = { "NUMBER", "NUMRANGE", "TEXT" })
	private String indicateDataType;
	
	private String indicateDataUnit;

}
