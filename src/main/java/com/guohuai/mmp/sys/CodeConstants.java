package com.guohuai.mmp.sys;

public class CodeConstants {
	// this.seqGenerator.next(CodeConstants.Publisher_offset)
	/**
	 * 第三方支付购买前缀 充值
	 */
	public static final String PAYMENT_deposit = "10";

	/**
	 * 第三方支付购买前缀 提现
	 */
	public static final String PAYMENT_withdraw = "11";

	/** 第三方支付购买前缀 购买 */
	public static final String PAYMENT_invest = "12";
	/**
	 * 第三方支付购买前缀 赎回
	 */
	public static final String PAYMENT_redeem = "13";

	/**
	 * 投资人轧差
	 */
	public static final String Investor_offset = "14";
	
	/**
	 * 第三方支付购买前缀 赎回
	 */
	public static final String special_redeem = "15";

	

	/**
	 * 第三方支付购买前缀 企业充值
	 */
	public static final String PAYMENT_debitDeposit = "16";

	/**
	 * 第三方支付购买前缀 企业提现
	 */
	public static final String PAYMENT_debitWithdraw = "17";
	
	/**
	 * 备付金--中间户代收
	 */
	public static final String Reserved_create_hosting_collect_trade = "18";
	
	/**
	 * 备付金--中间户代付
	 */
	public static final String Reserved_create_single_hosting_pay_trade = "19";
	
	/**
	 * 还本
	 */
	public static final String Publisher_repayLoan = "20";
	
	/**
	 * 付息
	 */
	public static final String Publisher_repayInterest = "21";
	
	/**
	 * 发行人--中间户代收
	 */
	public static final String Publisher_create_hosting_collect_trade = "22";
	
	/**
	 * 发行人--中间户代付
	 */
	public static final String Publisher_create_single_hosting_pay_trade = "23";
	
	/**
	 * 投资人 --批量代付--批次号
	 */
	public static final String Investor_batch_pay = "24";
	
	/**
	 * 产品编号
	 */
	public static final String Product_code = "80";
	
	/**
	 * 渠道编号
	 */
	public static final String Channel_code = "25";
	/**
	 * 渠道审批编号
	 */
	public static final String ChannelApprove_code = "26";
	/**
	 * 产品上下架编号
	 */
	public static final String ChannelProduct_code = "27";
	
	/**
	 * 委托单状态变化日志通知
	 */
	public static final String OrderLog_notifyId = "28";
	
	/**
	 * 委托单状态变化日志通知
	 */
	public static final String OrderLog_accoutingNotifyId = "29";
	
	/**
	 * 
	 */
	public static final String Publisher_repayCash = "30";
	
	public static final String Superacc_order = "31";
	
	/**
	 * 企业
	 */
	public static final String corAuditOrderNo = "32";
	
	/** SPV订单前缀 */
	public static final String SPV_order = "SPV";
	
	/** 渠道审批编号 */
	public static final String channelApproveCode = "CAC";
	
	/**清盘*/
	public static final String PAYMENT_clearRedeem = "33";
	/** 买卖单 */
	public static final String PAYMENT_buy = "34";
	
	
	/** 红包提现前缀 */
	public static final String INVESTOR_Coupon_withdraw = "35";
	public static final String INVESTOR_Coupon_batchPay = "36";
	
	/**
	 * 投资人 --退款批量代付--批次号
	 */
	public static final String Investor_batch_refund = "37";

	/**
	 * 机构用户扫尾
	 */
	public static final String PAYMENT_supplement = "99";
	
}
