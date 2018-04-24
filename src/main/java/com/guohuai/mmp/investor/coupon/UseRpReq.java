package com.guohuai.mmp.investor.coupon;

import com.guohuai.component.web.view.BaseRep;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 红包使用结果
 * 
 * @author wanglei
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString
public class UseRpReq extends BaseRep {

	/** 卡券编号 */
	String couponId;

	/** 卡券类型 */
	String couponType;

}
