package com.guohuai.ams.system.config.project.warrantyExpire;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
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
public class CCPWarrantyExpireForm {

	private String oid;
	@NotNull
	@NotBlank
	@NotEmpty
	@Length(max = 30, message = "担保期限长度不能超过30（包含）！")
	private String title;
	@Digits(integer = 4, fraction = 4)
	private BigDecimal weight100;

}
