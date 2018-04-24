package com.guohuai.mmp.investor.coupon;

import java.math.BigDecimal;

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
public class RedPacketsUseRep extends BaseRep {

	/** 卡券金额 */
	private BigDecimal couponAmount;
	
	private String couponOrderOid;

}
