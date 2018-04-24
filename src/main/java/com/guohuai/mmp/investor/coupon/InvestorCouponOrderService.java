package com.guohuai.mmp.investor.coupon;

import java.math.BigDecimal;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.channel.ChannelService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.platform.tulip.TulipConstants;
import com.guohuai.mmp.platform.tulip.TulipService;
import com.guohuai.mmp.platform.tulip.log.TulipLogEntity;
import com.guohuai.mmp.sys.CodeConstants;

@Service
public class InvestorCouponOrderService {
	Logger logger = LoggerFactory.getLogger(InvestorCouponOrderService.class);

	@Autowired
	private InvestorCouponOrderDao investorCouponOrderDao;
	@Autowired
	private TulipService tulipService;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	
	/**
	 * 第三方支付购买成功回调
	 */
	public static final String PAYMENT_trade_finished = "TRADE_FINISHED";
	public static final String PAYMENT_pay_finished = "PAY_FINISHED";
	

	/** 红包提现接口 */
	@Transactional(TxType.REQUIRES_NEW)
	public RedPacketsUseRep useRedPackets(String userOid, RedPacketsUseReq req) {

		RedPacketsUseRep rep = new RedPacketsUseRep();

		// 校验卡券信息
		this.tulipService.validateRedPacketsCoupon(userOid, req);
		// 创建订单
		InvestorCouponOrderEntity orderEntity = this.createEntity(userOid, req);
		// 锁定红包
		this.tulipService.lockCouponOfRedPackets(orderEntity);
		// 使用红包
		RedPacketsUseRep uRep = this.tulipService.useRp(req);
		String orderStatus = InvestorCouponOrderEntity.COUPON_orderStatus_toPay;
		BigDecimal couponAmount = uRep.getCouponAmount();
		
		if (uRep.getErrorCode() == 0) {
			// 调用新浪接口
			orderEntity.setCouponAmount(couponAmount);
			BaseRep baseRep = null;//this.investorPay(orderEntity);
			if (baseRep.getErrorCode() == 0) {
				
			} else {
				orderStatus = InvestorCouponOrderEntity.COUPON_orderStatus_submitFailed;
				
				this.tulipService.onCashOfRedPackets(orderEntity, TulipConstants.STATUS_ONCASH_FAIL, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CASH_FAIL_RP);
				
				rep.setErrorCode(uRep.getErrorCode());
				rep.setErrorMessage(uRep.getErrorMessage());
			}
		} else {
			rep.setErrorCode(uRep.getErrorCode());
			rep.setErrorMessage(uRep.getErrorMessage());
		}

		orderEntity.setCouponAmount(couponAmount);
		orderEntity.setOrderStatus(orderStatus);
		this.investorCouponOrderDao.save(orderEntity);

		
		rep.setCouponAmount(uRep.getCouponAmount());
		rep.setCouponOrderOid(orderEntity.getOid());
		return rep;
	}

	private InvestorCouponOrderEntity createEntity(String userOid, RedPacketsUseReq req) {

		InvestorCouponOrderEntity orderEntity = new InvestorCouponOrderEntity();
		orderEntity.setInvestorBaseAccount(this.investorBaseAccountService.findByUid(userOid));// 投资者基本账号
		orderEntity.setChannel(this.channelService.findOneByCid(req.getCid()));// 渠道信息
		orderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.INVESTOR_Coupon_withdraw));// 订单流水号
		orderEntity.setCoupons(req.getCouponId());// 卡券编号
		orderEntity.setCouponType(req.getCouponType());// 卡券类型
		orderEntity.setOrderTime(DateUtil.getSqlCurrentDate());// 订单时间
		orderEntity.setOrderStatus(InvestorCouponOrderEntity.COUPON_orderStatus_submitted);// 订单状态

		return this.investorCouponOrderDao.save(orderEntity);
	}

	/**
	 * 调用新浪支付申请
	 * 
	 * @param orderEntity
	 */
//	private BaseRep investorPay(InvestorCouponOrderEntity orderEntity) {
//
//		BigDecimal platformBalance = this.platformBalanceService.getMiddleAccount4RedeemCollect();
//		if (orderEntity.getCouponAmount().compareTo(platformBalance) > 0) {
//			this.logger.warn("balance({}) of platform middle account（还款专用） is not enough ", platformBalance);
//			throw AMPException.getException(20014);
//		}
//
//		InvestorPayRequest req = InvestorPayRequestBuilder.n().amount(orderEntity.getCouponAmount())
//				.outTradeNo(orderEntity.getOrderCode())
//				.payeeIdentityId(orderEntity.getInvestorBaseAccount().getUserOid()).summary(orderEntity.getCouponType())
//				.build();
//		return this.paymentServiceImpl.investorPay(req);
//
//	}

	/**
	 * 处理新浪支付回调
	 */
//	@Transactional(value = TxType.REQUIRES_NEW)
//	public void handleCallbackOrder(TradeStatusSync tradeStatus) {
//		InvestorCouponOrderEntity order = this.investorCouponOrderDao.findByOrderCode(tradeStatus.getOuter_trade_no());
//
//		// 成功
//		if (PAYMENT_trade_finished.equals(tradeStatus.getTrade_status())) {
//			/** 创建<<投资人-资金变动明细>> */
//			this.investorCashFlowService.createCashFlow(order);
//			/** 更新<<投资人-基本账户>>.<<余额>> */
//			//this.investorBaseAccountService.syncBalance(order.getInvestorBaseAccount());
//			
//			order.setOrderStatus(InvestorCouponOrderEntity.COUPON_orderStatus_done);
//			order.setCompleteTime(DateUtil.getSqlCurrentDate());// 订单完成时间
//			
//			// 发送推广平台红包使用成功通知
//			this.tulipService.onCashOfRedPackets(order, TulipConstants.STATUS_ONCASH_SUCC, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CASH_OK_RP);
//		} else if (PAYMENT_pay_finished.equals(tradeStatus.getTrade_status())) {
//			return;
//
//			// 失败
//		} else {
//			order.setOrderStatus(InvestorCouponOrderEntity.COUPON_orderStatus_payFailed);
//			// 发送推广平台红包使用失败通知
//			this.tulipService.onCashOfRedPackets(order, TulipConstants.STATUS_ONCASH_FAIL, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CASH_FAIL_RP);
//		}
//		
//		couponCashDetailsService.createEntity(order.getCouponAmount(), order.getCoupons());
//		this.investorCouponOrderDao.save(order);
//	}

	/**
	 * 卡券类型转码
	 * 
	 * @param coponType
	 * @return
	 */
	public String couponTypeEn2Ch(String coponType) {
		
		if (InvestorCouponOrderEntity.TYPE_couponType_redPackets.equals(coponType)) {
			return "优惠券(抵扣券)";
		}
		if (InvestorCouponOrderEntity.TYPE_couponType_tasteCoupon.equals(coponType)) {
			return "体验金";
		}
		if (InvestorCouponOrderEntity.TYPE_couponType_rateCoupon.equals(coponType)) {
			return "加息券";
		}
		return coponType;
	}
	
	public String couponTypeEn2ChWithBracket(String coponType) {
		
		if (StringUtil.isEmpty(coponType)) {
			return "";
		}

		if (InvestorCouponOrderEntity.TYPE_couponType_tasteCoupon.equals(coponType)) {
			return "(体验金)";
		}
		
		return coponType;
	}
	
	

	public String orderStatusEn2Ch(String orderStatus) {
		if (InvestorCouponOrderEntity.COUPON_orderStatus_submitted.equals(orderStatus)) {
			return "已申请";
		}
		if (InvestorCouponOrderEntity.COUPON_orderStatus_submitFailed.equals(orderStatus)) {
			return "申请失败";
		}
		if (InvestorCouponOrderEntity.COUPON_orderStatus_toPay.equals(orderStatus)) {
			return "待支付";
		}
		if (InvestorCouponOrderEntity.COUPON_orderStatus_payFailed.equals(orderStatus)) {
			return "支付失败";
		}
		if (InvestorCouponOrderEntity.COUPON_orderStatus_done.equals(orderStatus)) {
			return "支付成功";
		}
		return orderStatus;
	}

	public BaseRep isRedOkay(String couponOrderOid) {
		BaseRep baseRep = new BaseRep();
		InvestorCouponOrderEntity en = this.investorCouponOrderDao.findOne(couponOrderOid);
		if (InvestorCouponOrderEntity.COUPON_orderStatus_done.equals(en.getOrderStatus())) {
			
		} else if (InvestorCouponOrderEntity.COUPON_orderStatus_payFailed.equals(en.getOrderStatus())) {
			baseRep.setErrorCode(BaseRep.ERROR_CODE);
			baseRep.setErrorMessage("红包提现失败,请重新发起");
		} else {
			baseRep.setErrorCode(BaseRep.ERROR_CODE);
			baseRep.setErrorMessage("银行处理中...");
		}
		
		return baseRep;
	}
}
