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
 * 我的可购买某产品的卡券列表
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MyCouponOfProRep {

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

	/** 最小投资额,空表示无限制 */
	private BigDecimal minAmt;

	/** 最大投资额,空表示无限制 */
	private BigDecimal maxAmt;

	/** 最大加息金额 */
	private BigDecimal maxRateAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 加息天数 */
	private Integer rateDays;

	/** 使用规则 */
	private String rules = TulipConstants.BLANK;

	/** 使用产品,多个产品逗号分隔 */
	private String products = TulipConstants.BLANK;

	/** 失效时间 */
	private Date expiredDate;
	
}
