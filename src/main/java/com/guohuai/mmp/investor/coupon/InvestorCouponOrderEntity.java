package com.guohuai.mmp.investor.coupon;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.channel.Channel;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资者-红包使用记录
 * 
 * @author wanglei
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_COUPONORDER")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorCouponOrderEntity extends UUID {

	private static final long serialVersionUID = -7117285511185500013L;

	/** 订单状态--已申请 */
	public static final String COUPON_orderStatus_submitted = "submitted";
	/** 订单状态--申请失败 */
	public static final String COUPON_orderStatus_submitFailed = "submitFailed";
	/** 订单状态--待支付 */
	public static final String COUPON_orderStatus_toPay = "toPay";
	/** 订单状态--支付失败 */
	public static final String COUPON_orderStatus_payFailed = "payFailed";
	/** 订单状态--支付成功 */
	public static final String COUPON_orderStatus_done = "done";
	
	/** 推广平台卡券类型-优惠券(抵扣券) */
	public static final String TYPE_couponType_redPackets = "redPackets";
	/** 推广平台卡券类型-现金红包 */
	public static final String TYPE_couponType_cashCoupon = "cashCoupon";
	/** 推广平台卡券类型-体验金 */
	public static final String TYPE_couponType_tasteCoupon = "tasteCoupon";
	/** 推广平台卡券类型-加息券 */
	public static final String TYPE_couponType_rateCoupon = "rateCoupon";

	/** 所属投资人 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;

	/** 所属渠道 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "channelOid", referencedColumnName = "oid")
	private Channel channel;

	/** 订单号 */
	private String orderCode;

	/** 卡券编号 */
	private String coupons;

	/** 卡券类型 */
	private String couponType;

	/** 卡券金额 */
	private BigDecimal couponAmount;

	/** 订单状态 */
	private String orderStatus;

	/** 订单时间 */
	private Timestamp orderTime;

	/** 订单完成时间 */
	private Timestamp completeTime;

	private Timestamp createTime;

	private Timestamp updateTime;
}
