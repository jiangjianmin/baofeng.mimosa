package com.guohuai.mmp.platform.payment.log;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author yuechao
 *
 */
@lombok.Data
@lombok.Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PayLogReq implements Serializable {

	private static final long serialVersionUID = 5706029906665535170L;
	
	/**
	 * 接口名称
	 */
	private String interfaceName;
	/**
	 * 参数
	 */
	private String content;
	private String handleType;
	
	/** 已发送次数 */
	private Integer sendedTimes;
	
	private Integer errorCode;
	private String errorMessage;
	private String orderCode;
}
