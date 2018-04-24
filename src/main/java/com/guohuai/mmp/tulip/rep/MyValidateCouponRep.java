package com.guohuai.mmp.tulip.rep;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.coupon.InvestorCouponOrderEntity;
import com.guohuai.mmp.platform.tulip.TulipConstants;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 卡券校验结果
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MyValidateCouponRep extends BaseRep {

	/** 卡券抵扣金额 */
	private BigDecimal discount = SysConstant.BIGDECIMAL_defaultValue;

	/** 状态(参考TulipConstants中的定义) */
	private String status = TulipConstants.BLANK;

	/**
	 * 卡券类型(参考InvestorCouponOrderEntity中的定义)
	 * 
	 * @see InvestorCouponOrderEntity
	 */
	private String type = TulipConstants.BLANK;
}
