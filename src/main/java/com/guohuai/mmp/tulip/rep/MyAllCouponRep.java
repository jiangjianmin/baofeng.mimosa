package com.guohuai.mmp.tulip.rep;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

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
public class MyAllCouponRep {

	/** 卡券编号 */
	private String oid = TulipConstants.BLANK;

	/**
	 * 卡券类型(参考InvestorCouponOrderEntity中的定义)
	 * 
	 * @see InvestorCouponOrderEntity
	 */
	private String type = TulipConstants.BLANK;
	
	/**
	 * @Desc: 卡劵类型描述
	 * @author huyong
	 * @date 2017.08.04
	 */
	private String typeDesc = TulipConstants.BLANK;

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

	/** 使用规则 */
	private String rules = TulipConstants.BLANK;

	/** 使用产品,多个产品逗号分隔 */
	private String products = TulipConstants.BLANK;
	
	/** 最小投资额,空表示无限制 */
	private BigDecimal minAmt;

	/** 加息天数/体验天数 */
	private Integer rateDays;

	/** 使用时间 */
	private Timestamp useTime;

	/** 卡券领取时间 */
	private Timestamp leadTime;
}
