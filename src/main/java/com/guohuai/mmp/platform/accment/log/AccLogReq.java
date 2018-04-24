package com.guohuai.mmp.platform.accment.log;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 卡券校验请求
 * @author yuechao
 *
 */
@lombok.Data
@lombok.Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccLogReq implements Serializable {

	private static final long serialVersionUID = 5706029906665535170L;
	
	/**
	 * 接口名称
	 */
	private String interfaceName;
	/**
	 * 参数
	 */
	private String sendObj;
	
	/** 已发送次数 */
	private Integer sendedTimes;
	
	private Integer errorCode;
	private String errorMessage;
}
