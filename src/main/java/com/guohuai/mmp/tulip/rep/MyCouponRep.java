package com.guohuai.mmp.tulip.rep;

import java.math.BigDecimal;
import java.sql.Date;

import com.guohuai.mmp.investor.coupon.InvestorCouponOrderEntity;
import com.guohuai.mmp.platform.tulip.TulipConstants;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 推广平台券信息
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MyCouponRep {

	/** 卡券编号 */
	private String oid = TulipConstants.BLANK;

	/**
	 * 卡券类型(参考InvestorCouponOrderEntity中的定义)
	 * 
	 * @see InvestorCouponOrderEntity
	 */
	private String type = TulipConstants.BLANK;

	/** 卡券名称 */
	private String name = TulipConstants.BLANK;

	/** 卡券内容描述 */
	private String description = TulipConstants.BLANK;

	/** 卡券金额 */
	private BigDecimal amount = SysConstant.BIGDECIMAL_defaultValue;

	/** 生效时间 */
	private Date start;

	/** 失效时间 */
	private Date finish;

	/** 状态(参考TulipConstants中的定义) */
	private String status = TulipConstants.BLANK;

}
