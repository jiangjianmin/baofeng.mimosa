package com.guohuai.ams.system.config.project.warrantyLevel;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import com.guohuai.component.web.parameter.validation.Enumerations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CCPWarrantyLevelForm {

	private String oid;
	@NotNull
	@NotEmpty
	@NotBlank
	@Enumerations(values = { "", "NONE", "LOW", "MID", "HIGH" })
	private String wlevel;
	
	@NotNull
	@NotBlank
	@NotEmpty
	@Size(max = 32)
	private String name;

	@NotNull
	@NotBlank
	@NotEmpty
	@Size(max = 10)
	private String coverLow;
	@Digits(integer = 4, fraction = 4)
	private BigDecimal lowFactor;
	@Digits(integer = 4, fraction = 4)
	private BigDecimal highFactor;
	@NotNull
	@NotBlank
	@NotEmpty
	@Size(max = 10)
	private String coverHigh;

}
