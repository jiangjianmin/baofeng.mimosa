package com.guohuai.mmp.platform.tulip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.basic.cardvo.rep.CouponRep;
import com.guohuai.basic.cardvo.req.cardreq.ValidCardReq;
import com.guohuai.cardvo.service.MimosaService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.version.VersionUtils;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.bankorder.InvestorBankOrderEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountDao;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.log.CouponLogEntity;
import com.guohuai.mmp.investor.baseaccount.log.CouponLogReq;
import com.guohuai.mmp.investor.baseaccount.log.CouponLogService;
import com.guohuai.mmp.investor.coupon.InvestorCouponOrderEntity;
import com.guohuai.mmp.investor.coupon.RedPacketsUseRep;
import com.guohuai.mmp.investor.coupon.RedPacketsUseReq;
import com.guohuai.mmp.investor.tradeorder.FirstInvestReq;
import com.guohuai.mmp.investor.tradeorder.FirstInvestRes;
import com.guohuai.mmp.investor.tradeorder.FirstInvestService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderCouponEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderCouponService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.RedeemInvestTradeOrderReq;
import com.guohuai.mmp.investor.tradeorder.TradeOrderReq;
import com.guohuai.mmp.platform.reserved.couponcashdetails.CouponCashDetailsService;
import com.guohuai.mmp.platform.tulip.log.TuipLogReq;
import com.guohuai.mmp.platform.tulip.log.TulipLogEntity;
import com.guohuai.mmp.platform.tulip.log.TulipLogService;
import com.guohuai.mmp.sys.SysConstant;
import com.guohuai.mmp.tulip.rep.MyAllCouponRep;
import com.guohuai.mmp.tulip.rep.MyCouponOfProRep;
import com.guohuai.mmp.tulip.rep.MyRateCouponRep;
import com.guohuai.mmp.tulip.rep.OneCouponInfoRep;
import com.guohuai.mmp.tulip.req.TulipVerificationReq;
import com.guohuai.mmp.tulip.sdk.TulipSDKService;
import com.guohuai.tuip.api.TulipSdk;
import com.guohuai.tuip.api.objs.BaseObj;
import com.guohuai.tuip.api.objs.admin.CheckCouponRep;
import com.guohuai.tuip.api.objs.admin.CheckCouponReq;
import com.guohuai.tuip.api.objs.admin.CouponDetailRep;
import com.guohuai.tuip.api.objs.admin.CouponInterestRep;
import com.guohuai.tuip.api.objs.admin.CouponInterestReq;
import com.guohuai.tuip.api.objs.admin.EventRep;
import com.guohuai.tuip.api.objs.admin.InvestmentReq;
import com.guohuai.tuip.api.objs.admin.IssuedCouponReq;
import com.guohuai.tuip.api.objs.admin.MyCouponRep;
import com.guohuai.tuip.api.objs.admin.MyCouponReq;
import com.guohuai.tuip.api.objs.admin.OrderReq;
import com.guohuai.tuip.api.objs.admin.RefereeRep;
import com.guohuai.tuip.api.objs.admin.TulipListObj;
import com.guohuai.tuip.api.objs.admin.TulipObj;
import com.guohuai.tuip.api.objs.admin.UserReq;
import com.guohuai.tuip.api.objs.admin.VerificationCouponReq;
import com.guohuai.usercenter.api.UserCenterSdk;
import com.guohuai.usercenter.api.obj.RecommenderReq;

import lombok.extern.slf4j.Slf4j;

/**
 * 推广平台-业务实际处理接口<br/>
 * 
 * 
 * 注意:调用推广平台接口统一使用@see TulipNewService中的接口<br/>
 * mimosa代码中和推广平台接口重发定时器中，都会调用此service中的方法。
 *
 */
@Service
@Slf4j
public class TulipService {

	Logger logger = LoggerFactory.getLogger(TulipService.class);

	@Autowired
	private CouponCashDetailsService couponCashDetailsService;
	@Autowired
	private TulipSDKService tulipSDKService;
	@Autowired
	private TulipLogService tulipLogService;
	@Autowired
	private TulipSdk tulipSdk;
	@Autowired
	private CouponLogService couponLogService;
	@Autowired
	private UserCenterSdk userCenterSdk;
	@Autowired
	private FirstInvestService firstInvestService;
	@Autowired
	private TulipService tulipService;
	@Autowired
	private MimosaService mimosaService;
	@Autowired
	private InvestorBaseAccountDao investorBaseAccountDao;
	@Autowired
	private InvestorTradeOrderCouponService tradeOrderCouponService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private RedisTemplate<String, String> redis;

	/**
	 * 投资单接口中校验卡券是否可用于购买此产品
	 */
	public void validateCouponForInvest(TradeOrderReq tradeOrderReq) {
		VerifyCouponRequest vcReq = new VerifyCouponRequest();
		vcReq.setCouponId(tradeOrderReq.getCouponId());
		vcReq.setCouponType(tradeOrderReq.getCouponType());
		vcReq.setCouponAmount(tradeOrderReq.getCouponAmount());
		vcReq.setCouponDeductibleAmount(tradeOrderReq.getCouponDeductibleAmount());
		vcReq.setOrderAmount(tradeOrderReq.getMoneyVolume());
		vcReq.setPayAmouont(tradeOrderReq.getPayAmouont());
		vcReq.setProductOid(tradeOrderReq.getProductOid());
		vcReq.setOrderType(tradeOrderReq.getOrderType());
		vcReq.setRatio(tradeOrderReq.getRatio());
		vcReq.setRaiseDays(tradeOrderReq.getRaiseDays());
		vcReq.setDurationPeriodDays(tradeOrderReq.getDurationPeriodDays());
		validateCouponForInvest(vcReq);
	}
	
	/**
	 * 活转定接口中校验卡券是否可用于购买此产品
	 */
	public void validateCouponForRmInvest(RedeemInvestTradeOrderReq tradeOrderReq) {
		VerifyCouponRequest vcReq = new VerifyCouponRequest();
		vcReq.setCouponId(tradeOrderReq.getCouponId());
		vcReq.setCouponType(tradeOrderReq.getCouponType());
		vcReq.setCouponAmount(tradeOrderReq.getCouponAmount());
		vcReq.setCouponDeductibleAmount(tradeOrderReq.getCouponDeductibleAmount());
		vcReq.setOrderAmount(tradeOrderReq.getOrderAmount());
		vcReq.setPayAmouont(tradeOrderReq.getPayAmouont());
		vcReq.setProductOid(tradeOrderReq.getInvestProductOid());
		vcReq.setOrderType(tradeOrderReq.getOrderType());
		vcReq.setRatio(tradeOrderReq.getRatio());
		vcReq.setRaiseDays(tradeOrderReq.getRaiseDays());
		vcReq.setDurationPeriodDays(tradeOrderReq.getDurationPeriodDays());
		validateCouponForInvest(vcReq);
	}

	/**
	 * 投资单接口中校验卡券是否可用于购买此产品
	 */
	public void validateCouponForInvest(VerifyCouponRequest tradeOrderReq) {
		
		if (!this.isUseCoupon(tradeOrderReq.getCouponId())) {
			return;
		}

		if(!InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(tradeOrderReq.getOrderType())
				&& !InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(tradeOrderReq.getOrderType())
				&& !InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(tradeOrderReq.getOrderType())) {
//			// 校验此订单类型是否可用卡券
			throw new AMPException("此类型订单不允许使用卡券");
		}
		
		/**
		 * 体验金 couponDeductibleAmount = couponAmount = moneyVolume, payAmouont = 0
		 * 优惠券 couponDeductibleAmount + payAmount = moneyVolume, payAmount > 0, couponAmount >= couponDeductibleAmount 
		 * 加息券 ratio 代表加息利息， raiseDays 代表加息天数
		 */
		if (InvestorCouponOrderEntity.TYPE_couponType_tasteCoupon.equals(tradeOrderReq.getCouponType())) {
			/**
			 * 体验金 couponDeductibleAmount = couponAmount = moneyVolume,
			 * payAmouont = 0
			 */
			if (!this.isAmountEqualZero(tradeOrderReq.getPayAmouont())) {
				throw new AMPException("实付金额应为0");
			}

			if (tradeOrderReq.getOrderAmount().compareTo(tradeOrderReq.getCouponDeductibleAmount()) != 0) {
				throw new AMPException("卡券实际抵扣金额不等于订单金额");
			}
			if (tradeOrderReq.getOrderAmount().compareTo(tradeOrderReq.getCouponAmount()) != 0) {
				throw new AMPException("卡券金额不等于订单金额");
			}
			if (tradeOrderReq.getCouponAmount().compareTo(tradeOrderReq.getCouponDeductibleAmount()) != 0) {
				throw new AMPException("卡券实际抵扣金额不等于卡券金额");
			}

		} else if (InvestorCouponOrderEntity.TYPE_couponType_rateCoupon.equals(tradeOrderReq.getCouponType())) {
			if (!isAmountGreatThanZero(tradeOrderReq.getRatio())) {
				throw new AMPException("加息利率应大于0");
			}

			if (tradeOrderReq.getRaiseDays() <= 0) {
				throw new AMPException("加息天数应大于0");
			}
		} else if (InvestorCouponOrderEntity.TYPE_couponType_redPackets.equals(tradeOrderReq.getCouponType())) {
			/**
			 * 红包：couponDeductibleAmount + payAmount = moneyVolume, payAmount > 0, couponAmount >= couponDeductibleAmount
			 */
			if (!this.isAmountGreatThanZero(tradeOrderReq.getCouponAmount())) {
				throw new AMPException("卡券金额应大于0");
			}
			if (!isAmountGreatThanEqualZero(tradeOrderReq.getPayAmouont())) {
				throw new AMPException("实付金额应大于等于0");
			}
			if (tradeOrderReq.getCouponDeductibleAmount().compareTo(tradeOrderReq.getCouponAmount()) > 0) {
				// error.define[110009]=优惠券实际抵扣金额不能大于优惠券自身金额
				throw new AMPException(110009);
			}

			if (tradeOrderReq.getPayAmouont().add(tradeOrderReq.getCouponDeductibleAmount())
					.compareTo(tradeOrderReq.getOrderAmount()) != 0) {
				// error.define[110010]=优惠券实际抵扣金额与实付金额之和应等于订单金额
				throw new AMPException(110010);
			}
			// 校验本卡券是否可用于本产品（红包不能用于活期产品）
//			List<Product> redPackProduct = productDao.findRedPackProduct(tradeOrderReq.getProductOid());
//			if (redPackProduct.isEmpty()) {
//				// error.define[170001]=红包不可用于产品
//				throw new AMPException(170001);
//			}
			
		} else {
			// error.define[110015]=非法的优惠券类型
			throw new AMPException(110015);
		}
		
		if((InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(tradeOrderReq.getOrderType()) 
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(tradeOrderReq.getOrderType()))
				&& InvestorCouponOrderEntity.TYPE_couponType_tasteCoupon.equals(tradeOrderReq.getCouponType())) {
			throw new AMPException("此订单不可使用体验金");
		}
		if(InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(tradeOrderReq.getOrderType())
				&& (InvestorCouponOrderEntity.TYPE_couponType_rateCoupon.equals(tradeOrderReq.getCouponType())
				|| InvestorCouponOrderEntity.TYPE_couponType_redPackets.equals(tradeOrderReq.getCouponType()))) {
			throw new AMPException("体验金投资不可使用卡券");
		}
	}
	

	
	/**
	 * 锁定券(投资订单锁定卡券)
	 */
	public BaseRep lockCoupon(InvestorTradeOrderEntity orderEntity) {
		BaseRep rep=new BaseRep();
		if (!this.isUseCoupon(orderEntity.getCoupons())) {
			return rep;
		}
		
		TuipLogReq lreq = new TuipLogReq();
		
		if (Stream.of(InvestorCouponOrderEntity.TYPE_couponType_redPackets,
				InvestorCouponOrderEntity.TYPE_couponType_rateCoupon
				).anyMatch(c->Objects.equals(c, orderEntity.getCouponType()))) {
			/** 调用tulip校验红包相关参数 */
			ValidCardReq validCardReq = new ValidCardReq();
			validCardReq.setInvestorOid(orderEntity.getInvestorBaseAccount().getOid());
			validCardReq.setCouponOid(orderEntity.getCoupons());
			validCardReq.setProductOid(orderEntity.getProduct().getOid());
			validCardReq.setOrderAmount(orderEntity.getOrderAmount());
			validCardReq.setCouponAmount(orderEntity.getCouponAmount());
			validCardReq.setInvestDays(orderEntity.getProduct().getDurationPeriodDays());
			validCardReq.setRatio(orderEntity.getRatio()); 
			validCardReq.setCouponType(orderEntity.getCouponType());
			validCardReq.setRaiseDays(orderEntity.getRaiseDays());
			validCardReq.setIsFirst(isFirstTn(validCardReq.getInvestorOid())); // 是否首次投资定期产品（不包括新手标、精彩活动关联的产品）
			validCardReq.setOrderCode(orderEntity.getOrderCode());
			/** 调用tulip校验红包接口 */
			logger.info("验券请求: validCardReq=={}", validCardReq);
			CouponRep redPackRep;
			if (VersionUtils.checkVersionV160()) {
				logger.info("lockCoupon-old-validMyRedPack");
				redPackRep = this.tulipSdk.validMyRedPack(validCardReq);
			} else {
				logger.info("lockCoupon-new-validMyCoupon");
				redPackRep = this.tulipSdk.validMyCoupon(validCardReq);
			}
			logger.info("验券结果: errorCode==" + redPackRep.getErrorCode() + " errorMessage==" + redPackRep.getErrorMessage() + " result==" + redPackRep);
			lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CHECKREDPACK.getInterfaceName());
			lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CHECKREDPACK.getInterfaceCode());
			lreq.setSendObj(JSONObject.toJSONString(validCardReq));
			lreq.setErrorCode(rep.getErrorCode());
			lreq.setErrorMessage(rep.getErrorMessage());
			lreq.setSendedTimes(1);
			this.tulipLogService.createTulipLogEntity(lreq);
			if(redPackRep.getErrorCode() != 0) {
				throw new AMPException("网络异常，请稍候重试");
			} else {
				if((AMPException.CONCURRENT_CODE+"").equals(redPackRep.getStatus())) {
					throw new AMPException(AMPException.CONCURRENT_CODE, redPackRep.getDesc());
				} else if(!"200".equals(redPackRep.getStatus())) {
					throw new AMPException(redPackRep.getDesc());
				}
			}
			if(InvestorCouponOrderEntity.TYPE_couponType_rateCoupon.equals(orderEntity.getCouponType())) {
				//保存订单加息券关系表
				InvestorTradeOrderCouponEntity couponOrderEntity = new InvestorTradeOrderCouponEntity();
				couponOrderEntity.setOrderOid(orderEntity.getOid());
				couponOrderEntity.setInterest(orderEntity.getRatio());
				couponOrderEntity.setInterestDays(Math.min(orderEntity.getProduct().getDurationPeriodDays(), orderEntity.getRaiseDays()));
				tradeOrderCouponService.saveTradeOrderCoupon(couponOrderEntity);
			}
		} else {
			CheckCouponReq oreq = new CheckCouponReq();
			oreq.setProductId(orderEntity.getProduct().getOid());// 产品oid
			oreq.setCouponAmount(orderEntity.getCouponAmount()); // 卡券金额
			oreq.setCouponType(orderEntity.getCouponType()); // 卡券类型
			oreq.setCouponId(orderEntity.getCoupons());// 卡券编号
			oreq.setUserId(orderEntity.getInvestorBaseAccount().getUserOid());// 投资者
			oreq.setOrderCode(orderEntity.getOrderCode());// 订单流水
			CheckCouponRep orep = new CheckCouponRep();
			
			try {
				orep = this.tulipSdk.checkCoupon(oreq);
				log.info(JSONObject.toJSONString(orep));
				setIrep(rep, orep);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				rep.setErrorCode(BaseRep.ERROR_CODE);
				rep.setErrorMessage(AMPException.getStacktrace(e));
			}
			lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CHECKCOUPON.getInterfaceName());
			lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CHECKCOUPON.getInterfaceCode());
			lreq.setSendObj(JSONObject.toJSONString(oreq));
			lreq.setErrorCode(rep.getErrorCode());
			lreq.setErrorMessage(rep.getErrorMessage());
			lreq.setSendedTimes(1);
			this.tulipLogService.createTulipLogEntity(lreq);
		}
		if (0 != rep.getErrorCode()) {
			throw new AMPException(rep.getErrorMessage());
		}
		return rep;
	}
	
	/**
	* 是否首次投资定期产品（不包括新手标、精彩活动关联的产品）
	*/
	public boolean isFirstTn(String investorOid) {
		return investorTradeOrderService.isFirstTn(investorOid);
	}
	
	/**
	 * 向推广平台发送申购成功事件
	 */
	public void sendInvestOK(InvestorTradeOrderEntity orderEntity) {
		InvestmentReq ireq = new InvestmentReq();
		ireq.setOrderStatus(TulipParams.Status.SUCCESS.toString());
		this.sendInvest(orderEntity, ireq, true);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void sendInvestFailInNewTransaction(String orderCode) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		sendInvestFail(orderEntity);
	}
	
	/**
	 * 向推广平台发送申购失败事件
	 */
	public void sendInvestFail(InvestorTradeOrderEntity orderEntity) {
		InvestmentReq ireq = new InvestmentReq();
		ireq.setOrderStatus(TulipParams.Status.FAIL.toString());
		this.sendInvest(orderEntity, ireq, true);
	}
	
	public void sendInvest(InvestorTradeOrderEntity orderEntity, InvestmentReq ireq,boolean isLog) {
		ireq.setOrderType(orderEntity.getOrderType());
		ireq.setOrderCode(orderEntity.getOrderCode());
		ireq.setOrderAmount(orderEntity.getOrderAmount());
		ireq.setUserAmount(orderEntity.getPayAmount());
		ireq.setCreateTime(orderEntity.getOrderTime());
		ireq.setProductId(orderEntity.getProduct().getOid());
		ireq.setProductName(orderEntity.getProduct().getName());
		ireq.setUserId(orderEntity.getInvestorBaseAccount().getUserOid());
		ireq.setCouponId(orderEntity.getCoupons());
		ireq.setDiscount(orderEntity.getCouponAmount());
		ireq.setDurationPeriodDays(orderEntity.getProduct().getDurationPeriodDays());
		ireq.setCalcBaseDays(Integer.parseInt(orderEntity.getProduct().getIncomeCalcBasis()));
		if (StrRedisUtil.get(redis, StrRedisUtil.ORDERCODE_VERSION+orderEntity.getOrderCode()) != null) {
			ireq.setVersion(StrRedisUtil.get(redis, StrRedisUtil.ORDERCODE_VERSION+orderEntity.getOrderCode()));
			StrRedisUtil.del(redis, StrRedisUtil.ORDERCODE_VERSION+orderEntity.getOrderCode());
		}
		if (orderEntity.getCouponType() != null) {
			ireq.setCouponType(orderEntity.getCouponType());
		}
		if (orderEntity.getChannel() != null) {
			ireq.setChannelCid(orderEntity.getChannel().getCid());
		}
		this.sendInvestment(ireq, isLog);
	}
	
	public void sendInvestment(InvestmentReq req, boolean isLog) {
		BaseRep irep = new BaseRep();
		TulipObj orep = new TulipObj();
		try {
			if (TulipParams.Status.SUCCESS.toString().equals(req.getOrderStatus())) {
				
				// 订单成功后，如果订单中使用了红包，则调用tulip使用红包接口
				if (req.getCouponType() != null && (InvestorCouponOrderEntity.TYPE_couponType_redPackets.equals(req.getCouponType()) || InvestorCouponOrderEntity.TYPE_couponType_rateCoupon.equals(req.getCouponType()))) {
					ValidCardReq validCardReq = new ValidCardReq();
					validCardReq.setInvestorOid(req.getUserId());
					validCardReq.setCouponOid(req.getCouponId());
					validCardReq.setCouponType(req.getCouponType());
					validCardReq.setPayAmount(req.getUserAmount());
					validCardReq.setOrderAmount(req.getOrderAmount());
					validCardReq.setProductOid(req.getProductId());
//					validCardReq.setProductCode(mimosaService.getProductCodeByOid(req.getProductId()));
					validCardReq.setProductName(req.getProductName());
					validCardReq.setOrderCode(req.getOrderCode());
					validCardReq.setOrderTime(req.getCreateTime());
					validCardReq.setDurationPeriodDays(req.getDurationPeriodDays());
					validCardReq.setCalcBaseDays(req.getCalcBaseDays());;
					logger.info("sendInvestment-couponType:{}, version:{}", req.getCouponType(), req.getVersion());
					if (!StringUtils.isBlank(req.getVersion()) && VersionUtils.getCompareVersion(req.getVersion()) < VersionUtils.V160) {
						logger.info("sendInvestment-old-useMyRedPack");
						this.tulipSdk.useMyRedPack(validCardReq);
					} else {
						logger.info("sendInvestment-new-useMyCoupon");
						this.tulipSdk.useMyCoupon(validCardReq);
					}
				}
				
				CouponLogReq entity = new CouponLogReq();
				entity.setUserOid(req.getUserId());
				entity.setType(CouponLogEntity.TYPE_INVEST);
				entity.setChannelCid(req.getChannelCid());
				couponLogService.createEntity(entity);
				
				if (!InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(req.getOrderType())) {
					log.info("非普通投资，邀请人不发券！order type:{}", req.getOrderType());
				} else {
					/* 查询投资人的投资次数，只有被邀请且是首投的情况下，邀请人才下发体验金 */
					FirstInvestRes firstRes = new FirstInvestRes();
					FirstInvestReq firstReq = new FirstInvestReq();
					firstReq.setUserOid(req.getUserId());
					firstReq.setOrderCode(req.getOrderCode());
					firstInvestService.firstInvest(firstReq, firstRes);
					log.info("==用户{}首投判断ErrorCode{}==", req.getUserId(), firstRes.getErrorCode());

					RecommenderReq recommenderReq = new RecommenderReq();
					recommenderReq.setUserId(req.getUserId());
					String[] result = userCenterSdk.findRecommender(recommenderReq);
					log.info("推荐人信息：" + result.toString());
					if ("0".equals(firstRes.getErrorCode()) && result.length > 0) {
						String refereeId = result[0];
						log.info("投资回调，推荐人refereeId：" + refereeId);

						InvestorBaseAccountEntity account = investorBaseAccountDao.findByUserOid(req.getUserId());
						InvestorBaseAccountEntity recommender = investorBaseAccountDao.findByUserOid(refereeId);
						// (注册事件)推广平台注册事件
						this.tulipService.onReferee(account, recommender, req.getOrderCode());
						entity = new CouponLogReq();
						entity.setUserOid(refereeId);
						entity.setType(CouponLogEntity.TYPE_REFEREE);
						entity.setChannelCid(req.getChannelCid());
						couponLogService.createEntity(entity);
						logger.info("支付回调给用户：{}使用体验金券", req.getUserId());
					} else {
						log.info("投资人不是首次投资，推荐人不予下发体验金");
					}
				}
			}
			orep = this.tulipSdk.onInvestment(req);
			setIrep(irep, orep);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		if (isLog) {
			writeLog(req, irep, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_INVESTMENT.getInterfaceCode(),TulipLogEntity.TULIP_TYPE.TULIP_TYPE_INVESTMENT.getInterfaceName());
		}
	}
	
	private void writeLog(InvestmentReq ireq, BaseRep irep,String interfaceCode, String interfaceName) {
		this.writeLog(ireq, irep.getErrorCode(), irep.getErrorMessage(),interfaceCode, interfaceName);
	}

	
	/**
	 * 向推广平台发送注册事件
	 */
	public void onRegister(InvestorBaseAccountEntity investorAccount, InvestorBaseAccountEntity friendAccount) {
		
		UserReq req = new UserReq();
		req.setUserId(investorAccount.getUserOid());
		req.setPhone(investorAccount.getPhoneNum());
		req.setRegisterChannelId(investorAccount.getRegisterChannelId());
		if (null !=  friendAccount) {
			req.setFriendId(friendAccount.getUserOid());
		}

		this.onRegister(req);
	}
	
	public void onRegister(UserReq req) {
		this.onRegister(req, true);
	}

	public void onRegister(UserReq req, boolean isLog) {
		BaseRep irep = new BaseRep();
		TulipObj orep = new TulipObj();

		try {
			orep = this.tulipSdk.onRegister(req);
			setIrep(irep, orep);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
	
		if (isLog) {
			writeLog(req, irep, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REGISTER.getInterfaceCode(),TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REGISTER.getInterfaceName());
		}
	}
	
	private void writeLog(UserReq ireq, BaseRep irep,String interfaceCode, String interfaceName) {
		this.writeLog(ireq, irep.getErrorCode(), irep.getErrorMessage(), interfaceCode , interfaceName);
	}

	private void setIrep(BaseRep irep, BaseObj orep) {
		if (null == orep) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage("返回NULL");
		} else if (0 != orep.getErrorCode()) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(orep.getErrorMessage() + "(" + orep.getErrorCode() + ")");
		}
	}
	
	/**
	 * 向推广平台发送实名认证事件
	 */
	public void onSetRealName(InvestorBaseAccountEntity baseAccount) {
		UserReq ireq = new UserReq();
		ireq.setUserId(baseAccount.getUserOid());
		ireq.setName(baseAccount.getRealName());
		ireq.setPhone(baseAccount.getPhoneNum());
		if (null != baseAccount.getIdNum() && baseAccount.getIdNum().length() > 16) {
			String birthStr=baseAccount.getIdNum().substring(6, 14);
			ireq.setBirthday(DateUtil.formatUtilToSql(DateUtil.parseDate(birthStr, "yyyyMMdd")));
			
		}
	
		this.onSetRealName(ireq);
	}
	
	public void onSetRealName(UserReq ireq) {
		onSetRealName(ireq, true);
	}
	
	public void onSetRealName(UserReq ireq, boolean isLog) {
		BaseRep irep = new BaseRep();
		TulipObj orep = new TulipObj();
		try {
			orep = this.tulipSdk.onSetRealName(ireq);
			setIrep(irep, orep);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		if (isLog) {
			writeLog(ireq, irep, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_SETREALNAME.getInterfaceCode(),TulipLogEntity.TULIP_TYPE.TULIP_TYPE_SETREALNAME.getInterfaceName());
		}
	}
	
	private void writeLog(Object req, int errorCode, String errorMessage, String interfaceCode,String interfaceName) {
		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(errorCode);
		lreq.setErrorMessage(errorMessage);
		lreq.setInterfaceName(interfaceName);
		lreq.setInterfaceCode(interfaceCode);
		lreq.setSendObj(JSONObject.toJSONString(req));
		lreq.setSendedTimes(1);
		this.tulipLogService.createTulipLogEntity(lreq);
	}
	
	/**
	 * 发送赎回事件
	 */
	public void onRedeem(InvestorTradeOrderEntity orderEntity) {
		OrderReq ireq = new OrderReq();
		ireq.setUserId(orderEntity.getInvestorBaseAccount().getUserOid());
		ireq.setProductId(orderEntity.getProduct().getOid());
		ireq.setProductName(orderEntity.getProduct().getName());
		ireq.setOrderCode(orderEntity.getOrderCode());
		ireq.setOrderType(orderEntity.getOrderType());
		ireq.setOrderAmount(orderEntity.getOrderAmount());
		ireq.setCreateTime(orderEntity.getOrderTime());
		ireq.setDiscount(orderEntity.getCouponAmount());
		this.onRedeem(ireq);
	}
	
	public void onRedeem(OrderReq ireq) {
		this.onRedeem(ireq, true);
	}
	
	public void onRedeem(OrderReq ireq, boolean isLog) {
		BaseRep irep = new BaseRep();
		TulipObj orep = new TulipObj();
		try {
			orep = this.tulipSdk.onRedeem(ireq);
			setIrep(irep, orep);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		if (isLog) {
			writeLog(ireq, irep, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REDEEM.getInterfaceCode() , TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REDEEM.getInterfaceName());
		}
	}
	/**
	 * 向推广平台发送提现事件(从余额直接体现)
	 * 
	 */
	public void onWithdraw(InvestorBankOrderEntity bankOrder) {
		OrderReq ireq = new OrderReq();
		ireq.setOrderCode(bankOrder.getOrderCode());
		ireq.setCreateTime(bankOrder.getOrderTime());
		ireq.setUserId(bankOrder.getInvestorBaseAccount().getUserOid());
		ireq.setDiscount(BigDecimal.ZERO);
		ireq.setOrderAmount(bankOrder.getOrderAmount());
		ireq.setOrderAmount(bankOrder.getOrderAmount());
		ireq.setOrderStatus(TulipParams.Status.SUCCESS.toString());
		this.onWithdraw(ireq);
	}
	
	
	public void onWithdraw(OrderReq ireq) {
		this.onWithdraw(ireq, true);
	}
	
	public void onWithdraw(OrderReq ireq, boolean isLog) {
		BaseRep irep = new BaseRep();
		TulipObj orep = new TulipObj();
		try {
			orep = this.tulipSdk.onCash(ireq);
			setIrep(irep, orep);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		
		if (isLog) {
			writeLog(ireq, irep, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CASH.getInterfaceCode() ,TulipLogEntity.TULIP_TYPE.TULIP_TYPE_CASH.getInterfaceName());
		}
	}
	
	private void writeLog(OrderReq ireq, BaseRep irep,String interfaceCode, String interfaceName) {
		this.writeLog(ireq, irep.getErrorCode(), irep.getErrorMessage() , interfaceCode , interfaceName);
	}
	
	
	/** 推广平台事件(退款事件,到期兑付事件,赎回事件)处理 */
	public BaseRep tulipEventDeal(InvestorTradeOrderEntity order) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_refund.equals(order.getOrderType())) {

			// 退款事件监听
			return this.onRefund(order);

		} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(order.getOrderType())) {

			// 到期兑付事件监听(还本付息)
			return this.onBearer(order);

		} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(order.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_fastRedeem.equals(order.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_clearRedeem.equals(order.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(order.getOrderType())) {

			// (清盘赎回)赎回(快赎、普赎)完成到账 Q wl
			//return this.onRedeem(order);
		} 
		return new BaseRep();
		
	}
	

	/** 校验红包信息 */
	public void validateRedPacketsCoupon(String userOid, RedPacketsUseReq req) {
		
		
		this.tulipSDKService.assertSdkEnable();
		// 卡券编号不能为空
		if (!this.isUseCoupon(req.getCouponId())) {
			// error.define[110020]=卡券编号不能为空
			throw new AMPException(110020);
		}
		// 卡券类型暂时只支持红包提现
		if (!InvestorCouponOrderEntity.TYPE_couponType_rateCoupon.equals(req.getCouponType())) {
			// error.define[110021]=当前仅支持红包提现
			throw new AMPException(110021);
		}
	}
	
	/**
	 * 锁定券(红包提现锁定卡券)
	 * 
	 * @return
	 */
	public void lockCouponOfRedPackets(InvestorCouponOrderEntity orderEntity) {
		
		CheckCouponReq req = new CheckCouponReq();
		
		req.setCouponId(orderEntity.getCoupons());// 卡券编号
		req.setCouponType(orderEntity.getCouponType()); //卡券类型
		req.setUserId(orderEntity.getInvestorBaseAccount().getUserOid());// 投资者
		
		
		
	
		CheckCouponRep rep = this.tulipSDKService.checkCoupon(req, TulipLogEntity.TULIP_TYPE.TULIP_TYPE_LOCK_RP);
		if (0 != rep.getErrorCode()) {
			throw new AMPException(rep.getErrorCode() + "--" + rep.getErrorMessage());
		}
	}



	

	/**
	 * 卡券备付金清算时，卡券核销<br/>
	 * (返回码0推广平台返回成功，110002推广平台返回失败,110001请求推广平台异常)
	 * 
	 * @param verifacationsInfos
	 *            :需要核销的卡券信息
	 * 
	 * @return 核销结果(返回码0成功，110002失败,110001请求异常。仅此3个返回码)
	 */
	public BaseRep verificationCouponForLiquidation(TulipVerificationReq... verifacationsInfos) {

		TulipListObj<VerificationCouponReq> req = new TulipListObj<VerificationCouponReq>();
		List<VerificationCouponReq> list = new ArrayList<VerificationCouponReq>();
		if (verifacationsInfos != null && verifacationsInfos.length > 0) {
			for (TulipVerificationReq mimosaReq : verifacationsInfos) {

				VerificationCouponReq tulipReq = new VerificationCouponReq();
				// 订单流水号
				tulipReq.setOrderCode(mimosaReq.getOrderCode());
				// 订单金额
				tulipReq.setOrderAmount(getValue(mimosaReq.getOrderAmount()));
				// 实付金额
				tulipReq.setUserAmount(getValue(mimosaReq.getPayAmount()));
				// 卡券编号
				tulipReq.setCouponId(mimosaReq.getCouponId());
				// 卡券金额
				tulipReq.setDiscount(getValue(mimosaReq.getCouponAmount()));
				// 产品名称
				tulipReq.setProductName(mimosaReq.getProductName());
				// 产品oid
				tulipReq.setProductId(mimosaReq.getProOid());

				list.add(tulipReq);
			}
			req.setRows(list);
			req.setTotal(verifacationsInfos.length);
		}

		return this.verificationCouponCommon(req, true);

	}

	/**
	 * 卡券核销请求重发时，调用核销接口
	 * 
	 * @param req
	 *            :需要核销的卡券信息
	 * 
	 * @return 核销结果(返回码0成功，110002失败,110001请求异常。仅此3个返回码)
	 */
	public BaseRep verificationCouponForResend(TulipListObj<VerificationCouponReq> req) {
		return this.verificationCouponCommon(req, false);
	}

	/**
	 * 卡券核销SDK公共入口<br/>
	 * 1.卡券清算时，需要调用核销接口 <br/>
	 * 2.卡券平台核销接口重发时，也要调用核销接口<br/>
	 * 3.根据核销结果，更新卡券核销明细表的卡券核销状态
	 */
	private BaseRep verificationCouponCommon(TulipListObj<VerificationCouponReq> req, boolean saveTulipSdkLog) {

		// 请求推广平台结果
//		TulipObj tulipResult = this.tulipSDKService.verificationCoupon(req);
		this.tulipSDKService.verificationCoupon(req);
		
		// 更新卡券核销明细状态
		BaseRep baseRep = updateVerficationStatus(req);

		// 返回核销结果
		return baseRep;
	}

	/** 更新卡券核销明细状态 */
	private BaseRep updateVerficationStatus(TulipListObj<VerificationCouponReq> tulipParam) {

		// 卡券编码列表
		List<String> couponIdList = new ArrayList<String>();
		if (tulipParam != null && tulipParam.getRows() != null) {
			for (VerificationCouponReq req : tulipParam.getRows()) {
				couponIdList.add(req.getCouponId());// 卡券ID
			}
		}

		
		couponCashDetailsService.cancelDo(couponIdList);
		

		return new BaseRep();
	}

	/**
	 * 计算加息金额
	 * 
	 * @param userOid
	 *            :投资者编号
	 * @param couponID
	 *            :卡券编号
	 * @param orderCode
	 *            :订单流水号
	 * @param orderAmount
	 *            :订单金额
	 * @param days
	 *            :定期产品存续期天数
	 * @param incomeCalcBasis
	 *            :收益计算基础(360或365计息)
	 * @return
	 */
	protected MyRateCouponRep countRateCoupon(String userOid, String couponID, String orderCode, BigDecimal orderAmount,
			int days, int incomeCalcBasis) {
		MyRateCouponRep rep = new MyRateCouponRep();
		
		CouponInterestReq req = new CouponInterestReq();
		req.setUserId(userOid);
		req.setCouponId(couponID);
		req.setOrderCode(orderCode);
		req.setOrderAmount(orderAmount);
		req.setDays(days);
		req.setYearDay(incomeCalcBasis);

		CouponInterestRep tulipResult = this.tulipSDKService.couponInterest(req);
	
		
		// 推广平台返回成功时，获取其余参数信息
		if (tulipResult.getErrorCode() == TulipConstants.ERRORCODE_MIMOSA_0) {
			CouponInterestRep tulip = ((CouponInterestRep) tulipResult);
			rep.setRateAmount(tulip.getCouponAmount());// 加息金额
		}

		return rep;

	}

	

	/**
	 * 获取个人的券码列表（分页）
	 * 
	 * @param userOid
	 *            :投资者Oid
	 * @param status
	 *            :(可送空)卡券状态,unused-未使用,use-已使用,writeOff-核销,expired-过期
	 * @param type
	 *            :(可送空)卡券类型,redPackets-红包,coupon-优惠券,rateCoupon-加息券,tasteCoupon-体验金
	 * @param page
	 *            : 页号
	 * @param rows
	 *            :分页大小
	 */
	public PagesRep<MyAllCouponRep> getMyAllCouponList(String userOid, String status, String type, int page,
			int rows) {

		MyCouponReq req = new MyCouponReq();
		req.setStatus(status);
		req.setUserId(userOid);
		req.setPage(page);
		req.setRows(rows);
		req.setType(type);
		
		// 发送请求
		TulipListObj<MyCouponRep> tulipResult = tulipSDKService.getMyCouponList(req);
		
		PagesRep<MyAllCouponRep> pageResp = new PagesRep<MyAllCouponRep>();
		if (tulipResult.getErrorCode() == TulipConstants.ERRORCODE_MIMOSA_0) {
			if (tulipResult.getRows() != null) {
				for (MyCouponRep tulipObj : tulipResult.getRows()) {
					MyAllCouponRep entity = new MyAllCouponRep();
					entity.setOid(tulipObj.getOid());
					entity.setName(tulipObj.getName());// 卡券名称
					entity.setType(tulipObj.getType());// 卡券类型
//					entity.setTypeDesc(tulipObj.getTypeDesc());// 卡券类型描述
					entity.setDescription(tulipObj.getDescription());// 描述
					entity.setAmount(tulipObj.getAmount());// 金额
					entity.setMinAmt(tulipObj.getInvestAmount()); //最低投资额
					entity.setStart(tulipObj.getStart());// 生效时间
					entity.setFinish(tulipObj.getFinish());// 失效时间
					entity.setStatus(tulipObj.getStatus());// 状态
					entity.setRules(tulipObj.getRules());// 使用规则
					entity.setProducts(tulipObj.getProducts());// 适用产品，逗号分隔
					entity.setRateDays(tulipObj.getValidPeriod());// 最大优惠天数（加息天数）
					entity.setUseTime(tulipObj.getUseTime());// 使用时间
					entity.setLeadTime(tulipObj.getLeadTime());// 领用时间
					
					pageResp.add(entity);
				}
				pageResp.setTotal(tulipResult.getTotal());
			}
		}

		return pageResp;
	}

	/**
	 * 获取个人可用于某产品的券码列表
	 * 
	 * @param userOid
	 *            :投资者Oid
	 * @param proOid
	 *            :(可送空)产品Oid
	 * @param investmentAmount
	 *            :(可送空)申购金额
	 */
	protected PagesRep<MyCouponOfProRep> getMyCouponListOfPro(String userOid, String proOid) {

		 MyCouponReq req = new MyCouponReq();
	        req.setUserId(userOid);
	        req.setProductId(proOid);
	        TulipListObj<MyCouponRep> tulipResult = tulipSDKService.getCouponList(req);
	        PagesRep<MyCouponOfProRep> pageResp = new PagesRep<MyCouponOfProRep>();
	        
	        if (tulipResult.getErrorCode() == TulipConstants.ERRORCODE_MIMOSA_0) {
	            if (tulipResult.getRows() != null) {
	                for (MyCouponRep tulipObj : tulipResult.getRows()) {
	                    MyCouponOfProRep entity = new MyCouponOfProRep();
	                    entity.setOid(tulipObj.getOid());
	                    entity.setName(tulipObj.getName());// 卡券名称
	                    entity.setType(tulipObj.getType());// 卡券类型
	                    entity.setDescription(tulipObj.getDescription());// 描述
	                    entity.setAmount(tulipObj.getAmount());// 金额
	                    entity.setMinAmt(tulipObj.getInvestAmount());// 单笔最小投资
	                    entity.setMaxRateAmount(tulipObj.getMaxRateAmount());// 最大加息金额
	                    entity.setRateDays(tulipObj.getValidPeriod());// 最大加息天数
	                    entity.setRules(tulipObj.getRules());// 使用规则
	                    entity.setProducts(tulipObj.getProducts());// 适用产品
	                    entity.setExpiredDate(tulipObj.getFinish());// 有效截止日期
	                    pageResp.add(entity);
	                }
	                pageResp.setTotal(tulipResult.getTotal());
	            }
	        }
	        return pageResp;
	}

	/**
	 * 获取某个券信息
	 */
	protected OneCouponInfoRep getCouponDetail(String couponId) {

		MyCouponReq req = new MyCouponReq();
		req.setCouponId(couponId);

		CouponDetailRep tulipRep = this.tulipSDKService.getCouponDetail(req);

		OneCouponInfoRep rep = new OneCouponInfoRep();
		// 推广平台返回成功时，获取其余参数信息
		if (tulipRep.getErrorCode() == TulipConstants.ERRORCODE_MIMOSA_0) {
			rep.setUserId(tulipRep.getUserId());// 用户编号;
			rep.setOrderCode(tulipRep.getOrderCode());// 订单流水
			rep.setDiscount(tulipRep.getDiscount());// 卡券金额
			rep.setValidPeriod(tulipRep.getValidPeriod());// 最大优惠天数/最多体验天数
			rep.setMaxRateAmount(tulipRep.getMaxRateAmount());// 最大优惠金额/最大收益金额
		}

		return rep;
	}

	
	
	public RedPacketsUseRep useRp(RedPacketsUseReq req) {
		RedPacketsUseRep baseRep = this.tulipSDKService.useRp(req);
		
		return baseRep;
	}
	
	

	



	/**
	 * 下发卡券
	 * 
	 * @param userOid
	 *            :用户oid
	 * @param eventId
	 *            :活动ID
	 */
	public BaseRep issuedCoupon(String userOid, String eventId) {
		BaseRep rep = new BaseRep();
		IssuedCouponReq req = new IssuedCouponReq();
		req.setUserId(userOid);// 用户oid
		req.setEventId(eventId);// 活动ID

		// 请求推广平台结果
		TulipObj tulipResult = this.tulipSDKService.issuedCoupon(req);
		rep.setErrorCode(tulipResult.getErrorCode());
		rep.setErrorMessage(tulipResult.getErrorMessage());
		
		return rep;

		
	}

	
	/**
	 * 向推广平台发送推荐人事件
	 * 
	 * @param account
	 *            :注册人
	 * @param recommender
	 *            :推荐人
	 */
	public BaseRep onReferee(InvestorBaseAccountEntity account, InvestorBaseAccountEntity recommender, String orderCode) {
		
		UserReq req = new UserReq();
		req.setUserId(account.getUserOid());
		req.setPhone(account.getPhoneNum());// 手机号
		if (recommender != null) {
			req.setFriendId(recommender.getUserOid());// 推荐人
		}
		req.setOrderCode(orderCode);
		// 请求推广平台结果
		BaseRep rep = this.tulipSDKService.onReferee(req);
		
		return rep;
	}
	

	
	



	/**
	 * 向推广平台发送到期兑付事件
	 */
	public BaseRep onBearer(InvestorTradeOrderEntity order) {
		
		OrderReq req = new OrderReq();
		req.setUserId(order.getInvestorBaseAccount().getUserOid());// 投资者oid
		req.setDiscount(order.getCouponAmount());// 卡券抵扣金额
		req.setOrderCode(order.getOrderCode());// 订单流水号
		req.setOrderType(order.getOrderType());// 订单类型
		req.setOrderAmount(order.getOrderAmount());// 订单金额
		req.setCreateTime(order.getCreateTime());// 订单创建时间
		req.setProductId(order.getProduct().getOid());// 产品oid
		req.setProductName(order.getProduct().getName());// 产品名称

	
		BaseRep rep = this.tulipSDKService.onBearer(req);
		
		return rep;
	}

	/**
	 * 向推广平台发送红包提现事件
	 * 
	 */
	public BaseRep onCashOfRedPackets(InvestorCouponOrderEntity order, String status, TulipLogEntity.TULIP_TYPE type) {
		BaseRep rep = new BaseRep();
		
		OrderReq req = new OrderReq();
		req.setOrderCode(order.getOrderCode());// 订单流水号
		req.setCreateTime(order.getCompleteTime());// 订单时间
		req.setUserId(order.getInvestorBaseAccount().getUserOid());// 用户oid
		req.setCouponId(order.getCoupons());// 卡券编号
		req.setOrderStatus(status);// 提现结果，成功/失败

		// 请求推广平台结果
		TulipObj tulipResult = this.tulipSDKService.onCash(req, type);
		rep.setErrorCode(tulipResult.getErrorCode());
		rep.setErrorMessage(tulipResult.getErrorMessage());
		
		return rep;
	}


	/**
	 * 向推广平台发送退款事件
	 */
	public BaseRep onRefund(InvestorTradeOrderEntity order) {
		BaseRep rep = new BaseRep();
		OrderReq req = new OrderReq();
		req.setOrderCode(order.getOrderCode());
		req.setUserId(order.getInvestorBaseAccount().getUserOid());

		// 请求推广平台结果
		TulipObj tulipResult = this.tulipSDKService.onRefund(req);
		rep.setErrorCode(tulipResult.getErrorCode());
		rep.setErrorMessage(tulipResult.getErrorMessage());
		
		TuipLogReq lreq = new TuipLogReq();
		lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REFUND.getInterfaceName());
		lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REFUND.getInterfaceCode());
		lreq.setErrorCode(tulipResult.getErrorCode());
		lreq.setErrorMessage(tulipResult.getErrorMessage());
		lreq.setSendedTimes(1);
		lreq.setSendObj(JSONObject.toJSONString(req));
		this.tulipLogService.createTulipLogEntity(lreq);
		return rep;
	}

	/**
	 * 判断是否使用了卡券
	 * 
	 * @param couponId
	 *            :卡券编号
	 * @return
	 */
	public boolean isUseCoupon(String couponId) {
		return StringUtil.isEmpty(couponId) ? false : true;
	}

	
	/**
	 * 金额是否大于0,如果 大于0，返回true,否则返回false
	 */
	private boolean isAmountGreatThanZero(BigDecimal amount) {
		if (null == amount) {
			return false;
		}
		return amount.compareTo(BigDecimal.ZERO) > 0;
	}
	
	/**
	 * 金额是否大于等于0,如果 大于等于0，返回true,否则返回false
	 */
	public boolean isAmountGreatThanEqualZero(BigDecimal amount) {
		if (null == amount) {
			return false;
		}
		if (amount.compareTo(BigDecimal.ZERO) >= 0) {
			return true;
		}
		return false;
	}
	/**
	 * 金额是否等于0,如果 等于0，返回true,否则返回false
	 */
	public boolean isAmountEqualZero(BigDecimal amount) {
		if (null == amount) {
			return false;
		}
		if (amount.compareTo(BigDecimal.ZERO) == 0) {
			return true;
		}
		return false;
	}
	
	private BigDecimal getValue(BigDecimal b) {
		return b == null ? SysConstant.BIGDECIMAL_defaultValue : b;
	}
	   /**
     * 获取推荐人活动
     * @return
     */
    public EventRep getFriendEventInfo() {
        return tulipSDKService.getFriendEventInfo();
    }
    /**
     * 获取注册活动
     * @return
     */
    public EventRep getRegisterEventInfo() {
        return tulipSDKService.getRegisterEventInfo();
    }
    /**
     *根据用户ID获取推荐人ID
     * @param uid
     * @return
     */
    public RefereeRep getFriendIdByUid(String uid){
        return tulipSDKService.getFrendIdByUid(uid);
    }



	
}
