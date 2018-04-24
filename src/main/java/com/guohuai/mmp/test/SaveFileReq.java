package com.guohuai.mmp.test;

import java.io.Serializable;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;

@lombok.Data
public class SaveFileReq implements Serializable {

	private static final long serialVersionUID = 691022236113209974L;

	@NotBlank(message = "文件名不可为空")
	private String name;
	@NotBlank(message = "文件链接不可为空")
	private String furl;
	@Min(value = 1)
	private long size;
}
