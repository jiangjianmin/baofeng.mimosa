package com.guohuai.mmp.platform.errorlog;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 卡券校验请求
 *
 */
@lombok.Data
@lombok.Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorLogReq {

	private String reqUri;
	private String params;
}
