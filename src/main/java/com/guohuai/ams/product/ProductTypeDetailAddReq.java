package com.guohuai.ams.product;

import java.sql.Timestamp;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProductTypeDetailAddReq {
	private String id;
	@NotBlank(message = "type不可为空")
	@NotNull(message = "type不可为空")
	@NotEmpty(message = "type不可为空")
	private String type;
	@NotBlank(message = "title不可为空")
	@NotNull(message = "title不可为空")
	@NotEmpty(message = "title不可为空")
	private String title;
	@NotBlank(message = "detail不可为空")
	@NotNull(message = "detail不可为空")
	@NotEmpty(message = "detail不可为空")
	private String detail;
	private String url;
	private String operator;
	private Timestamp createTime;
	private Timestamp updateTime;
}
