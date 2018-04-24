package com.guohuai.ams.system.config.project.warrantyMode;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
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
public class CCPWarrantyModeForm {

	private String oid;
	@Enumerations(values = { "GUARANTEE", "MORTGAGE", "HYPOTHECATION" })
	private String type;
	@NotEmpty
	@NotNull
	@NotBlank
	@Length(max = 30, message = "担保方式名称长度不能超过30（包含）！")
	private String title;
	@Digits(integer = 4, fraction = 4)
	private BigDecimal weight100;

}
