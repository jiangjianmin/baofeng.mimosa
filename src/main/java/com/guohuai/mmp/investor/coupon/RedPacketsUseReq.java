package com.guohuai.mmp.investor.coupon;

import org.hibernate.validator.constraints.NotBlank;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 红包使用请求信息
 * 
 * @author wanglei
 *
 */
@lombok.Data
@NoArgsConstructor
@ToString
public class RedPacketsUseReq {

	String cid;

	String ckey;

	/** 卡券编号 */
	@NotBlank(message = "红包编号不能为空")
	String couponId;

	/** 卡券类型 */
	@NotBlank(message = "红包类型不能为空")
	String couponType;

}
