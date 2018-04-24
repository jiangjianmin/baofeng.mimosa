package com.guohuai.mmp.platform.publisher.offset;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class OffsetCloseReq {
	
	/**
	 * 轧差主键ID
	 */
	@NotBlank(message="轧差ID不能为空")
	String offsetOid;

	/**
	 * 返回URL
	 */
	@URL(message = "返回的URL格式错误")
	String returnUrl;

	String ip;
	
	String uid;
}
