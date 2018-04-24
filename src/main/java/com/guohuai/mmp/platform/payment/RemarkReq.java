package com.guohuai.mmp.platform.payment;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class RemarkReq implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String version; /* 版本号 */
}
