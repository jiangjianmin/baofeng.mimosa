package com.guohuai.mmp.tulip.rep;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;
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
public class OneCouponInfoRep extends BaseRep {

	/** 用户编号 */
	private String userId = TulipConstants.BLANK;

	/** 订单号 */
	private String orderCode = TulipConstants.BLANK;

	/** 卡券收益金额 */
	private BigDecimal discount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 最大优惠天数/最多体验天数 （0表示无限制）<br/>
	 * 
	 * <b>
	 * 注意:最大优惠天数和最大收益金额，两者同时设置时，要取2者收益中的较小的
	 * </b>
	 */
	private Integer validPeriod = SysConstant.INTEGER_defaultValue;

	/**
	 * 最大优惠金额/最大收益金额（0表示无限制）<br/>
	 * 
	 * 注意:最大优惠天数和最大收益金额，两者同时设置时，要取2者收益中的较小的
	 * 
	 */
	private BigDecimal maxRateAmount = SysConstant.BIGDECIMAL_defaultValue;
}
