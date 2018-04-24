package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckEntity;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetEntity;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资人-交易委托单
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_TRADEORDER")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorTradeOrderEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = 4333179226422640561L;

	// submitted:已申请，refused:已拒绝，toPay:待支付，payFailed:支付失败，paySuccess:支付成功，
	// payExpired:支付超时，accepted:已受理，confirmed:份额已确认，done:交易成功，refunded:已退款，abandoned:已作废',
	/** 订单状态--已提交 */
	public static final String TRADEORDER_orderStatus_submitted = "submitted";
	/** 订单状态--提交失败 */
	public static final String TRADEORDER_orderStatus_refused = "refused";
	/** 订单状态--待支付 */
	public static final String TRADEORDER_orderStatus_toPay = "toPay";
	/** 订单状态--支付回调失败 */
	public static final String TRADEORDER_orderStatus_payFailed = "payFailed";
	/** 订单状态--已支付 */
	public static final String TRADEORDER_orderStatus_paySuccess = "paySuccess";
	/** 订单状态--已过期 */
	public static final String TRADEORDER_orderStatus_payExpired = "payExpired";
	/** 订单状态--已受理 */
	public static final String TRADEORDER_orderStatus_accepted = "accepted";
	/** 订单状态--已确认 */
	public static final String TRADEORDER_orderStatus_confirmed = "confirmed";
	/** 订单状态--已成交 */
	public static final String TRADEORDER_orderStatus_done = "done";

	/** 订单状态--退款中 */
	public static final String TRADEORDER_orderStatus_refunding = "refunding";
	/** 订单状态--已退款 */
	public static final String TRADEORDER_orderStatus_refunded = "refunded";

	/** 订单状态--作废中 */
	public static final String TRADEORDER_orderStatus_abandoning = "abandoning";
	/** 订单状态--已作废 */
	public static final String TRADEORDER_orderStatus_abandoned = "abandoned";
	/** plus订单状态--已作废 */
	public static final String TRADEORDER_orderStatus_invalidate = "invalidate";

	/** 交易类型--体验金赎回 */
	public static final String TRADEORDER_orderType_expGoldRedeem = "expGoldRedeem";
	/** 交易类型--补赎回单 */
	public static final String TRADEORDER_orderType_reRedeem = "reRedeem";
	/** 交易类型--投资 */
	public static final String TRADEORDER_orderType_invest = "invest";
	/** 交易类型--活转定||定转活--申购 */
	public static final String TRADEORDER_orderType_noPayInvest = "noPayInvest";
	/** 交易类型--补投资单 */
	public static final String TRADEORDER_orderType_reInvest = "reInvest";
	/** 交易类型--体验金投资 */
	public static final String TRADEORDER_orderType_expGoldInvest = "expGoldInvest";
	/** 交易类型--普赎 */
	public static final String TRADEORDER_orderType_normalRedeem = "normalRedeem";
	/** 交易类型--快定宝赎回 */
	public static final String TRADEORDER_orderType_bfPlusRedeem = "bfPlusRedeem";
	/** 交易类型--特殊赎回 */
	public static final String TRADEORDER_orderType_specialRedeem = "specialRedeem";
	/** 交易类型--活转定--赎回 */
	public static final String TRADEORDER_orderType_noPayRedeem = "noPayRedeem";
	/** 交易类型--快赎 */
	public static final String TRADEORDER_orderType_fastRedeem = "fastRedeem";
	/** 交易类型--节节高赎回 */
	public static final String TRADEORDER_orderType_incrementRedeem = "incrementRedeem";
	/** 交易类型--清盘赎回 */
	public static final String TRADEORDER_orderType_clearRedeem = "clearRedeem";
	/** 交易类型--还本/付息 */
	public static final String TRADEORDER_orderType_cash = "cash";
	/** 交易类型--募集失败退款 */
	public static final String TRADEORDER_orderType_cashFailed = "cashFailed";
	/** 交易类型--还本 */
	public static final String TRADEORDER_orderType_repayLoan = "repayLoan";
	/** 交易类型--付息 */
	public static final String TRADEORDER_orderType_repayInterest = "repayInterest";
	/** 交易类型--退款 */
	public static final String TRADEORDER_orderType_refund = "refund";
	/** 交易类型--买卖(平台) */
	public static final String TRADEORDER_orderType_buy = "buy";
	/** 交易类型--冲销单 */
	public static final String TRADEORDER_orderType_writeOff = "writeOff";
	/** 交易类型--二级邀请奖励投资 */
	public static final String TRADEORDER_orderType_profitInvest = "profitInvest";

	/** 交易类型--循环产品转投 */
	public static final String TRADEORDER_orderType_changeInvest = "changeInvest";
	/** 交易类型--循环产品续投 */
	public static final String TRADEORDER_orderType_continueInvest = "continueInvest";

	/** 订单创建人--投资人 */
	public static final String TRADEORDER_createMan_investor = "investor";
	/** 订单创建人--平台 */
	public static final String TRADEORDER_createMan_platform = "platform";
	/** 订单创建人--发行人 */
	public static final String TRADEORDER_createMan_publisher = "publisher";

	/** 三方对账状态--已对 */
	public static final String TRADEORDER_checkStatus_yes = "yes";
	/** 三方对账状态--未对 */
	public static final String TRADEORDER_checkStatus_no = "no";

	/** 合同生成状态--等待生成html */
	public static final String TRADEORDER_contractStatus_toHtml = "toHtml";
	/** 合同生成状态--生成html成功 */
	public static final String TRADEORDER_contractStatus_htmlOK = "htmlOK";
	/** 合同生成状态--生成html失败 */
	public static final String TRADEORDER_contractStatus_htmlFail = "htmlFail";
	/** 合同生成状态--生成PDF成功 */
	public static final String TRADEORDER_contractStatus_pdfOK = "pdfOK";

	/** 发行人清算状态--待清算 */
	public static final String TRADEORDER_publisherClearStatus_toClear = "toClear";
	/** 发行人清算状态--清算中 */
	public static final String TRADEORDER_publisherClearStatus_clearing = "clearing";
	/** 发行人清算状态--已清算 */
	public static final String TRADEORDER_publisherClearStatus_cleared = "cleared";

	/** 发行人交收状态--待交收 */
	public static final String TRADEORDER_publisherConfirmStatus_toConfirm = "toConfirm";
	/** 发行人交收状态--交收中 */
	public static final String TRADEORDER_publisherConfirmStatus_confirming = "confirming";
	/** 发行人交收状态--已交收 */
	public static final String TRADEORDER_publisherConfirmStatus_confirmed = "confirmed";
	public static final String TRADEORDER_publisherConfirmStatus_confirmFailed = "confirmFailed";

	/** 发行人结算状态--待结算 */
	public static final String TRADEORDER_publisherCloseStatus_toClose = "toClose";
	/** 发行人结算状态--结算中 */
	public static final String TRADEORDER_publisherCloseStatus_closing = "closing";
	/** 发行人结算状态--已结算 */
	public static final String TRADEORDER_publisherCloseStatus_closed = "closed";
	/** 发行人结算状态--结算申请失败 */
	public static final String TRADEORDER_publisherCloseStatus_closeSubmitFailed = "closeSubmitFailed";
	/** 发行人结算状态--结算支付失败 */
	public static final String TRADEORDER_publisherCloseStatus_closePayFailed = "closePayFailed";

	/** 投资人清算状态--待清算 */
	public static final String TRADEORDER_investorClearStatus_toClear = "toClear";
	/** 投资人清算状态--清算中 */
	public static final String TRADEORDER_investorClearStatus_clearing = "clearing";
	/** 投资人清算状态--已清算 */
	public static final String TRADEORDER_investorClearStatus_cleared = "cleared";

	/** 投资人结算状态--待结算 */
	public static final String TRADEORDER_investorCloseStatus_toClose = "toClose";
	/** 投资人结算状态--结算中 */
	public static final String TRADEORDER_investorCloseStatus_closing = "closing";
	/** 投资人结算状态--已结算 */
	public static final String TRADEORDER_investorCloseStatus_closed = "closed";
	/** 投资人结算状态--结算申请失败 */
	public static final String TRADEORDER_investorCloseStatus_closeSubmitFailed = "closeSubmitFailed";
	/** 投资人结算状态--结算支付失败 */
	public static final String TRADEORDER_investorCloseStatus_closePayFailed = "closePayFailed";

	/** 可赎回状态-可以 */
	public static final String TRADEORDER_redeemStatus_yes = "yes";
	/** 可赎回状态-不可以 */
	public static final String TRADEORDER_redeemStatus_no = "no";

	/** 可计息状态-可以 */
	public static final String TRADEORDER_accrualStatus_yes = "yes";
	/** 可计息状态-不可以 */
	public static final String TRADEORDER_accrualStatus_no = "no";

	/** 持有状态-待确认 */
	public static final String TRADEORDER_holdStatus_toConfirm = "toConfirm";
	/** 持有状态-持有中 */
	public static final String TRADEORDER_holdStatus_holding = "holding";
	/** 持有状态-已到期 */
	public static final String TRADEORDER_holdStatus_expired = "expired";
	/** 持有状态-部分持有 */
	public static final String TRADEORDER_holdStatus_partHolding = "partHolding";
	/** 持有状态-已结算 */
	public static final String TRADEORDER_holdStatus_closed = "closed";
	/** 持有状态-已退款 */
	public static final String TRADEORDER_holdStatus_refunded = "refunded";
	/** 持有状态-已作废 */
	public static final String TRADEORDER_holdStatus_abandoned = "abandoned";

	/** 支付状态-待支付 */
	public static final String TRADEORDER_payStatus_toPay = "toPay";
	/** 支付状态-申请失败 */
	public static final String TRADEORDER_payStatus_submitFailed = "submitFailed";
	/** 支付状态-支付失败 */
	public static final String TRADEORDER_payStatus_payFailed = "payFailed";
	/** 支付状态-支付成功 */
	public static final String TRADEORDER_payStatus_paySuccess = "paySuccess";
	/** 支付状态-支付超时 */
	public static final String TRADEORDER_payStatus_payExpired = "payExpired";

	/** 受理状态-待受理 */
	public static final String TRADEORDER_acceptStatus_toAccept = "toAccept";
	/** 受理状态-已受理 */
	public static final String TRADEORDER_acceptStatus_accepted = "accepted";
	/** 受理状态-受理失败 */
	public static final String TRADEORDER_acceptStatus_acceptFailed = "acceptFailed";

	/** 退款状态-待退款 */
	public static final String TRADEORDER_refundStatus_toRefund = "toRefund";
	/** 退款状态-退款中 */
	public static final String TRADEORDER_refundStatus_refunding = "refunding";
	/** 退款状态-已退款 */
	public static final String TRADEORDER_refundStatus_refunded = "refunded";
	/** 退款状态-退款失败 */
	public static final String TRADEORDER_refundStatus_refundFailed = "refundFailed";

	/** 支付模式-代付 */
	public static final String TRADEORDER_paymentMode_agentPay = "agentPay";
	/** 支付模式-代扣 */
	public static final String TRADEORDER_paymentMode_withhold = "withhold";
	/** 支付模式-网关 */
	public static final String TRADEORDER_paymentMode_gateway = "gateway";

	/**
	 * 所属投资人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;

	/**
	 * 所属发行人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisherOid", referencedColumnName = "oid")
	private PublisherBaseAccountEntity publisherBaseAccount;

	/**
	 * 所属产品
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	private Product product;

	/**
	 * 所属渠道
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "channelOid", referencedColumnName = "oid")
	private Channel channel;

	/**
	 * 所属投资人轧差
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOffsetOid", referencedColumnName = "oid")
	private InvestorOffsetEntity investorOffset;

	/**
	 * 所属发行人轧差
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisherOffsetOid", referencedColumnName = "oid")
	private PublisherOffsetEntity publisherOffset;

	/**
	 * 所属三方对账
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "checkOid", referencedColumnName = "oid")
	private PlatformFinanceCheckEntity platformFinanceCheck;

	/**
	 * 发行人-持有人手册
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "holdOid", referencedColumnName = "oid")
	private PublisherHoldEntity publisherHold;

	/**
	 * 订单号
	 */
	private String orderCode;

	/**
	 * 订单类型
	 */
	private String orderType;

	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 订单份额
	 */
	private BigDecimal orderVolume = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 订单状态
	 */
	private String orderStatus;

	/**
	 * 三方对账状态
	 */
	private String checkStatus;

	/**
	 * 合同生成状态
	 */
	private String contractStatus;

	/**
	 * 订单创建人
	 */
	private String createMan;

	/**
	 * 订单时间
	 */
	private Timestamp orderTime;
	/**
	 * 订单完成时间
	 */
	private Timestamp completeTime;

	/**
	 * 发行人清算状态
	 */
	private String publisherClearStatus;

	/**
	 * 发行人交收状态
	 */
	private String publisherConfirmStatus;

	/***
	 * 发行人结算状态
	 */
	private String publisherCloseStatus;

	/**
	 * 投资人清算状态
	 */
	private String investorClearStatus;

	/**
	 * 投资人结算状态
	 */
	private String investorCloseStatus;

	/** 卡券编号 */
	private String coupons;

	/** 卡券类型 */
	private String couponType;

	/** 卡券金额 */
	private BigDecimal couponAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 实付金额
	 *
	 * <pre>
	 * 卡券类型为：红包、体验金、优惠券时，此字段单位为元，如12元；
	 * 卡券类型为：加息券时，此字段单位为百分比，实际计算时此百分比时要除以100。如加息5%时，数据库中此字段实际存储为5(不是0.05)。
	 * </pre>
	 */
	private BigDecimal payAmount = SysConstant.BIGDECIMAL_defaultValue;
	// 暴风新增字段
	/** 持有份额 */
	private BigDecimal holdVolume = SysConstant.BIGDECIMAL_defaultValue;
	/** 可赎回状态 */
	private String redeemStatus;
	/** 可计息状态 */
	private String accrualStatus;

	/**
	 * 本金计息截止日期
	 */
	private Date corpusAccrualEndDate;

	/** 起息日 */
	private Date beginAccuralDate;
	/** 起始赎回日 */
	private Date beginRedeemDate;
	/** 累计收益 */
	private BigDecimal totalIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 累计基础收益 */
	private BigDecimal totalBaseIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 累计奖励收益 */
	private BigDecimal totalRewardIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 昨日基础收益 */
	private BigDecimal yesterdayBaseIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 昨日奖励收益 */
	private BigDecimal yesterdayRewardIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 昨日收益 */
	private BigDecimal yesterdayIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 待结转收益 */
	private BigDecimal toConfirmIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 收益金额 */
	private BigDecimal incomeAmount = SysConstant.BIGDECIMAL_defaultValue;
	/** 预期收益Ext */
	private BigDecimal expectIncomeExt = SysConstant.BIGDECIMAL_defaultValue;
	/** 预期收益 */
	private BigDecimal expectIncome = SysConstant.BIGDECIMAL_defaultValue;
	/** 最新市值 */
	private BigDecimal value = SysConstant.BIGDECIMAL_defaultValue;
	/** 持有状态 */
	private String holdStatus;
	/** 收益确认日期 */
	private Date confirmDate;

	/** 支付状态 */
	private String payStatus;
	/** 受理状态 */
	private String acceptStatus;
	/** 退款状态 */
	private String refundStatus;

	private String province;
	private String city;

	private Timestamp createTime;

	private Timestamp updateTime;
	//对于定转活，定期流标，还本付息，需要存储订单关联关系
	private String relateOid;
	private String payChannel;
	private String isAuto;
    private String protocalOid;
	private Date payDate;
	@Transient
	private BigDecimal ratio;
	@Transient
	private Integer raiseDays;

}
