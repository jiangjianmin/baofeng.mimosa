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

/** 单张券信息 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MyLockCouponRep extends BaseRep {

	/** 订单支付金额 */
	private BigDecimal orderAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 用户支付金额 */
	private BigDecimal userAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 卡券抵扣金额 */
	private BigDecimal discount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 卡券类型(参考InvestorCouponOrderEntity中的定义)
	 * 
	 * @see InvestorCouponOrderEntity
	 */
	private String type = TulipConstants.BLANK;
}
