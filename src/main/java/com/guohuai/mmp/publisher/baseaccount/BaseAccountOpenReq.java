package com.guohuai.mmp.publisher.baseaccount;


import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseAccountOpenReq {
	@NotEmpty(message = "corperateOid不能为空")
	@Length(message = "corperateOid长度不能超过32个字符")
	String corperateOid;
	
	@NotEmpty(message = "publisherPayUid不能为空")
	@Length(message = "publisherPayUid长度不能超过32个字符")
	String publisherPayUid;
}
