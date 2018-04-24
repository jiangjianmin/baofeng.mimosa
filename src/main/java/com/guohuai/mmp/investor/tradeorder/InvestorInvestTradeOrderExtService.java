package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.ams.dict.Dict;
import com.guohuai.ams.guess.GuessInvestItemService;
import com.guohuai.ams.guess.GuessService;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.basic.message.MessageConstant;
import com.guohuai.cache.CacheKeyConstants;
import com.guohuai.cache.entity.ProductCacheEntity;
import com.guohuai.cache.service.CacheChannelService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.cache.service.RedisExecuteLogExtService;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.cardvo.dao.TradeOrderStatisticsDao;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.BeanUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.abandonlog.AbandonLogService;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountDao;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.orderlog.OrderLogEntity;
import com.guohuai.mmp.investor.orderlog.OrderLogService;
import com.guohuai.mmp.investor.tradeorder.check.CheckOrderReq;
import com.guohuai.mmp.platform.finance.modifyorder.ModifyOrderNewService;
import com.guohuai.mmp.platform.payment.InnerOrderRep;
import com.guohuai.mmp.platform.payment.OrderNotifyReq;
import com.guohuai.mmp.platform.payment.PayParam;
import com.guohuai.mmp.platform.redis.RedisSyncService;
import com.guohuai.mmp.platform.tulip.TulipService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.serialtask.*;
import com.guohuai.moonBox.FamilyEnum;
import com.guohuai.moonBox.service.FamilyInvestPlanService;
import com.guohuai.tuip.api.TulipSdk;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class InvestorInvestTradeOrderExtService {

	@Autowired
	private InvestorInvestTradeOrderService investorInvestTradeOrderService;
//	@Autowired
//	private InvestorOpenCycleService investorOpenCycleService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private OrderLogService orderLogService;
	@Autowired
	private TulipService tulipService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private SerialTaskRequireNewService serialTaskRequireNewService;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private CacheChannelService cacheChannelService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private ModifyOrderNewService modifyOrderNewService;
	@Autowired
	private AbandonLogService abandonLogService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private InvestorRedeemInvestTradeOrderService investorRedeemInvestTradeOrderService;
	@Autowired
	private ProductService productService;
	@Autowired
	private InvestorSpecialRedeemAuthService investorSpecialRedeemAuthService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private InvestorBaseAccountDao investorBaseAccountDao;
	@Autowired
	private RedisSyncService redisSyncService;
	@Autowired
	private RedisExecuteLogExtService redisExecuteLogExtService;
	@Autowired
	private GuessInvestItemService guessInvestItemService;
	@Autowired
	private FamilyInvestPlanService familyInvestPlanService;
	@Autowired
	private MessageSendUtil messageSendUtil;
	@Autowired
	private TulipSdk tulipSdk;
	@Autowired
	private ProductIncomeRewardCacheService rewardCacheService;
	@Autowired
	private GuessService guessService;
	@Autowired
	private TradeOrderStatisticsDao tradeOrderStatisticsDao;
	@Autowired
	private TradeCalendarService tradeCalendarService;
	@Value("${p2p.amount.limit:50000}")
	private BigDecimal limitAmount;


	
	@Transactional
	public TradeOrderRep expGoldInvest(TradeOrderReq tradeOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		
		/** 创建订单 */
		InvestorTradeOrderEntity orderEntity = investorInvestTradeOrderService.createExpGoldInvestTradeOrder(tradeOrderReq);
		
		try {
			tradeOrderRep = investorInvestTradeOrderService.investRequireNew(orderEntity.getOrderCode());
		} catch (Exception e) {
			log.error("体验金投资订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage(e.getMessage());
			if (!(e instanceof AMPException) || !AMPException.CONCURRENT_CODE.equals(((AMPException)e).getCode())) {
				this.tulipService.sendInvestFailInNewTransaction(orderEntity.getOrderCode());
			}
			
		}
		
		investorInvestTradeOrderService.investThen(tradeOrderRep, orderEntity.getOrderCode());
		return tradeOrderRep;
	}
	
	
	@Transactional
	public TradeOrderRep investValidate(TradeOrderReq tradeOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();

		if(StringUtils.isBlank(tradeOrderReq.getUid())){
			throw new AMPException(BaseRep.ERROR_CODE, "当前用户未登录或会话已超时");
		}
		InvestorBaseAccountEntity baseAccount = investorBaseAccountService.findByUid(tradeOrderReq.getUid());
		if(baseAccount == null || "forbidden".equals(baseAccount.getStatus())){
			throw new AMPException(BaseRep.ERROR_CODE, "当前用户被锁定，无法投资");
		}
		/** 校验渠道 */
		this.cacheChannelService.checkChannel(tradeOrderReq.getCid(), tradeOrderReq.getCkey(),
				tradeOrderReq.getProductOid());

		Product product = productService.getProductByOid(tradeOrderReq.getProductOid());
		InvestorTradeOrderEntity orderEntity = new InvestorTradeOrderEntity();
		InvestorBaseAccountEntity investorBaseAccountEntity = investorBaseAccountService.findByUid(tradeOrderReq.getUid());
		orderEntity.setInvestorBaseAccount(investorBaseAccountEntity);
		orderEntity.setOrderType(tradeOrderReq.getOrderType());
		orderEntity.setOrderAmount(tradeOrderReq.getMoneyVolume());
        orderEntity.setProduct(product);
		// 活期、定期正常投资，活转定
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())
				|| (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())
				&& Product.TYPE_Producttype_01.equals(product.getType()))) {
			//---------------增加判断超级用户判断--------20170413---------
			if (DecimalUtil.isGoRules(product.getInvestMin())) {
				if (!investorBaseAccountService.isSuperMan(orderEntity)) {
					//不是超级用户
					//--------定期尾单不校验产品投资最小金额且必须一次性买完------2017.06.26----
					if (!cacheProductService.checkIsLastTnOrder(orderEntity)) {
						//不是定期尾单
						if (orderEntity.getOrderAmount().compareTo(product.getInvestMin()) < 0) {
							// error.define[30008]=不能小于产品投资最低金额(CODE:30008)
							throw new AMPException(30008);
						}
					} else {
						//定期尾单，必须一次性买完
						if (orderEntity.getOrderAmount().compareTo(product.getMaxSaleVolume()) != 0) {

							throw new GHException("定期尾单，必须一次性买完");
						}
					}
					//--------定期尾单不校验产品投资最小金额且必须一次性买完------2017.06.26----

				}
			}
			if (DecimalUtil.isGoRules(product.getInvestMax())) {
				if (!investorBaseAccountService.isSuperMan(orderEntity)) {
					if (orderEntity.getOrderAmount().compareTo(product.getInvestMax()) > 0) {
						// error.define[30009]=已超过产品投资最高金额(CODE:30009)
						throw new AMPException(30009);
					}
				}
			}
			if (DecimalUtil.isGoRules(product.getInvestAdditional())) {
				//-------------------------超级用户--------------2017.04.17-----
				if (!investorBaseAccountService.isSuperMan(orderEntity)) {
					//--------不是定期尾单才校验产品投资追加金额------2017.06.26----
					if (!cacheProductService.checkIsLastTnOrder(orderEntity)) {
						if (DecimalUtil.isGoRules(product.getInvestMin())) {
							if (orderEntity.getOrderAmount().subtract(product.getInvestMin()).remainder(product.getInvestAdditional()).compareTo(BigDecimal.ZERO) != 0) {
								// error.define[30010]=不满足产品投资追加金额(CODE:30010)
								throw new AMPException(30010);
							}
						} else {
							if (orderEntity.getOrderAmount().remainder(product.getInvestAdditional()).compareTo(BigDecimal.ZERO) != 0) {
								// error.define[30010]=不满足产品投资追加金额(CODE:30010)
								throw new AMPException(30010);
							}
						}
					}
					//--------不是定期尾单才校验产品投资追加金额------2017.06.26----
				}
				//-------------------------超级用户--------------2017.04.17-----
			}
			if (DecimalUtil.isGoRules(product.getInvestMin())) {
				if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
					String productLabels = product.getProductLabel();
					if (this.labelService.isProductLabelHasAppointLabel(productLabels, LabelEnum.tiyanjin.toString())) {
						throw new AMPException(30072);
					}
				}
			}

		}

		return tradeOrderRep;
	}

	@Transactional
	public TradeOrderRep normalInvest(TradeOrderReq tradeOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();

		/** 校验产品交易时间 */
		cacheProductService.isInDealTime(tradeOrderReq.getProductOid());
		Product product = productService.getProductByOid(tradeOrderReq.getProductOid());
		BigDecimal totalAmount = publisherHoldService.getTotalHoldAmount(tradeOrderReq.getUid());
		if((Product.TYPE_Producttype_01.equals(product.getType().getOid()) || Product.TYPE_Producttype_03.equals(product.getType().getOid()))
				&& Product.NOT_P2P.equals(product.getIfP2P()) && totalAmount.compareTo(limitAmount) < 0) {
			tradeOrderRep.setErrorCode(-1);
			tradeOrderRep.setErrorMessage("当前产品已经售罄，感谢您的支持");
			return tradeOrderRep;
		}
		InvestorBaseAccountEntity baseAccount = investorBaseAccountService.findByUid(tradeOrderReq.getUid());
		if(baseAccount == null || "forbidden".equals(baseAccount.getStatus())) {
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage("当前用户被锁定，无法投资");
			return tradeOrderRep;
		}
		/** 校验渠道 */
		this.cacheChannelService.checkChannel(tradeOrderReq.getCid(), tradeOrderReq.getCkey(),
				tradeOrderReq.getProductOid());
		
		/** 创建订单 */
		InvestorTradeOrderEntity orderEntity = investorInvestTradeOrderService.createNormalInvestTradeOrderRequireNew(tradeOrderReq);
		/**
		 *
		 *申购开放循环产品判断逻辑
		 * @author yujianlong
		 * @date 2018/3/20 17:12
		 */
		String productType=Optional.ofNullable(orderEntity.getProduct()).map(Product::getType).map(Dict::getOid).orElse("");

		if(!Objects.equals(Product.TYPE_Producttype_03,productType)){

			/**
			 * 如果产品关联了竞猜宝，如果没有传选项答案，报错
			 */
			if(guessService.hasRelatedGuess(tradeOrderReq.getProductOid())){
				if(StringUtils.isBlank(tradeOrderReq.getGuessItemOid())){
					throw new GHException("关联竞猜活动的产品必须传入选项oid");
				}
			}

			/** 竞猜宝的话创建活动投资选项 */
			if(StringUtils.isNoneBlank(tradeOrderReq.getGuessItemOid())){
				BaseRep rep = guessInvestItemService.choose(tradeOrderReq,orderEntity);
				log.info("竞猜宝的话创建活动投资选项返回:{}",rep);
			}
		}else{
			//插入快定宝关系记录表
			investorInvestTradeOrderService.createInvestorOpenCycleRelation(orderEntity);
		}
		try {
			tradeOrderRep = investorInvestTradeOrderService.investRequireNew(orderEntity.getOrderCode(), tradeOrderReq.getRatio(), tradeOrderReq.getRaiseDays());
		} catch (Exception e) {
			log.error("投资订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			if (e instanceof GHException) {
				tradeOrderRep.setErrorMessage(e.getMessage());
			} else {
				tradeOrderRep.setErrorMessage("系统繁忙，请稍后重试！");
			}
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			redisSyncService.investRedisRevert(orderEntity.getOrderCode());
//			redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), orderEntity.getProduct().getOid(), orderEntity.getProduct().getAssetPool().getOid());
			//并发异常不需要发送失败
			if (!(e instanceof AMPException) || !AMPException.CONCURRENT_CODE.equals(((AMPException)e).getCode())) {
				this.tulipService.sendInvestFailInNewTransaction(orderEntity.getOrderCode());
			}
		}
		investorInvestTradeOrderService.investThen(tradeOrderRep, orderEntity.getOrderCode());

		return tradeOrderRep;
	}


	/**
	 * 网关支付申购
	 * @param tradeOrderGatewayReq
	 * @return
	 */
	@Transactional
	public TradeOrderGatewayRep normalInvestThroughGateway(TradeOrderGatewayReq tradeOrderGatewayReq) {
		TradeOrderGatewayRep tradeOrderGatewayRep = new TradeOrderGatewayRep();
		Product product = productService.getProductByOid(tradeOrderGatewayReq.getProductOid());
		BigDecimal totalAmount = publisherHoldService.getTotalHoldAmount(tradeOrderGatewayReq.getUid());
		if((Product.TYPE_Producttype_01.equals(product.getType().getOid()) || Product.TYPE_Producttype_03.equals(product.getType().getOid()))
                && Product.NOT_P2P.equals(product.getIfP2P()) && totalAmount.compareTo(limitAmount) < 0) {
			tradeOrderGatewayRep.setErrorCode(-1);
			tradeOrderGatewayRep.setErrorMessage("当前产品已经售罄，感谢您的支持");
			return tradeOrderGatewayRep;
		}
		/** 校验产品交易时间 */
		cacheProductService.isInDealTime(tradeOrderGatewayReq.getProductOid());

		InvestorBaseAccountEntity baseAccount = investorBaseAccountService.findByUid(tradeOrderGatewayReq.getUid());
		if(baseAccount == null || "forbidden".equals(baseAccount.getStatus())) {
			tradeOrderGatewayRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderGatewayRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderGatewayRep.setErrorMessage("当前用户被锁定，无法投资");
			return tradeOrderGatewayRep;
		}
		/** 校验渠道 */
		this.cacheChannelService.checkChannel(tradeOrderGatewayReq.getCid(), tradeOrderGatewayReq.getCkey(),
				tradeOrderGatewayReq.getProductOid());

		/** 创建订单 */
		InvestorTradeOrderEntity orderEntity = investorInvestTradeOrderService.createNormalInvestTradeOrderRequireNew(tradeOrderGatewayReq);

		/**
		 * 如果产品关联了竞猜宝，如果没有传选项答案，报错
		 */
		if(guessService.hasRelatedGuess(tradeOrderGatewayReq.getProductOid())){
			if(StringUtils.isBlank(tradeOrderGatewayReq.getGuessItemOid())){
				throw new GHException("关联竞猜活动的产品必须传入选项oid");
			}
		}

		/** 竞猜宝的话创建活动投资选项 */
		if(StringUtils.isNoneBlank(tradeOrderGatewayReq.getGuessItemOid())){
			BaseRep rep = guessInvestItemService.choose(tradeOrderGatewayReq,orderEntity);
			log.info("竞猜宝的话创建活动投资选项返回:{}",rep);
		}
		try {
			tradeOrderGatewayRep = investorInvestTradeOrderService.investRequireNewThroughGateway(orderEntity.getOrderCode(), tradeOrderGatewayReq);
		} catch (Exception e) {
			log.error("投资订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			tradeOrderGatewayRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderGatewayRep.setErrorMessage(e.getMessage());
			tradeOrderGatewayRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			redisSyncService.investRedisRevert(orderEntity.getOrderCode());
			//并发异常不需要发送失败
			if (!(e instanceof AMPException) || !AMPException.CONCURRENT_CODE.equals(((AMPException)e).getCode())) {
				this.tulipService.sendInvestFailInNewTransaction(orderEntity.getOrderCode());
			}
		}

		TradeOrderRep investThenRep = new TradeOrderRep();
		BeanUtil.copy(investThenRep, tradeOrderGatewayRep);

		investorInvestTradeOrderService.investThen(investThenRep, orderEntity.getOrderCode());

		return tradeOrderGatewayRep;
	}
	
	
	/**
	 * 活转定
	 */
	@Transactional
	public TradeOrderRep redeemInvest(RedeemInvestTradeOrderReq tradeOrderReq, String userOid) {
		TradeOrderRep tradeOrderRep = validateInvestProductAndCoupon(tradeOrderReq);
		/** 创建订单 */
		InvestorTradeOrderEntity orderEntity = investorInvestTradeOrderService.createNoPayInvestTradeOrderRequireNew(tradeOrderReq, userOid);
		Product product = productService.getProductByOid(tradeOrderReq.getInvestProductOid());
		BigDecimal totalAmount = publisherHoldService.getTotalHoldAmount(userOid);
		if((Product.TYPE_Producttype_01.equals(product.getType().getOid()) || Product.TYPE_Producttype_03.equals(product.getType().getOid()))
                && Product.NOT_P2P.equals(product.getIfP2P()) && totalAmount.compareTo(limitAmount) < 0) {
			tradeOrderRep.setErrorCode(-1);
			tradeOrderRep.setErrorMessage("当前产品已经售罄，感谢您的支持");
			return tradeOrderRep;
		}
		/**
		 *
		 *申购开放循环产品判断逻辑
		 * @author yujianlong
		 * @date 2018/3/20 17:12
		 */
		String productType=Optional.ofNullable(orderEntity.getProduct()).map(Product::getType).map(Dict::getOid).orElse("");

		if(!Objects.equals(Product.TYPE_Producttype_03,productType)){

			/**
			 * 如果产品关联了竞猜宝，如果没有传选项答案，报错
			 */
			if(guessService.hasRelatedGuess(tradeOrderReq.getInvestProductOid())){
				if(StringUtils.isBlank(tradeOrderReq.getGuessItemOid())){
					throw new GHException("关联竞猜活动的产品必须传入选项oid");
				}
			}

			/** 竞猜宝的话创建活动投资选项 */
			if(StringUtils.isNoneBlank(tradeOrderReq.getGuessItemOid())){
				BaseRep rep = guessInvestItemService.choose(tradeOrderReq,orderEntity);
				log.info("竞猜宝的话创建活动投资选项返回:{}",rep);
			}
		}else{
			//插入快定宝关系记录表
			investorInvestTradeOrderService.createInvestorOpenCycleRelation(orderEntity);
		}
		try {
			RedeemTradeOrderReq redeemReq = new RedeemTradeOrderReq();
			redeemReq.setCid(tradeOrderReq.getCid());
			redeemReq.setCkey(tradeOrderReq.getCkey());
			redeemReq.setOrderAmount(orderEntity.getPayAmount());
			redeemReq.setUid(userOid);
			redeemReq.setProductOid(tradeOrderReq.getRedeemProductOid());

			tradeOrderRep = investorRedeemInvestTradeOrderService.rmInvest(orderEntity.getOrderCode(), tradeOrderReq.getRatio(), tradeOrderReq.getRaiseDays(),redeemReq);
		} catch (Exception e) {
			log.error("活转定订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			if (e instanceof GHException) {
				tradeOrderRep.setErrorMessage(e.getMessage());
			} else {
				tradeOrderRep.setErrorMessage("系统繁忙，请稍后重试！");
			}
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			if (!(e instanceof AMPException) || !AMPException.CONCURRENT_CODE.equals(((AMPException)e).getCode())) {
				this.tulipService.sendInvestFailInNewTransaction(orderEntity.getOrderCode());
			}

		}


		investorInvestTradeOrderService.investThen(tradeOrderRep, orderEntity.getOrderCode());

		return tradeOrderRep;
	}
	
	/**
	 * 活转活
	 */
	@Transactional
	public TradeOrderRep transferInvest(RedeemInvestTradeOrderReq tradeOrderReq, String userOid) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();

		List<ProductIncomeReward> rewardList = rewardCacheService.readCache(tradeOrderReq.getInvestProductOid());
		if(rewardList.isEmpty() || !Product.TYPE_Producttype_02.equals(rewardList.get(0).getProduct().getType().getOid())) {
			tradeOrderRep.setErrorCode(-1);
			tradeOrderRep.setErrorMessage("转入的产品必须是天天向上");
			return tradeOrderRep;
		}
		InvestorTradeOrderEntity investOrder = investorInvestTradeOrderService.createNoPayInvestTradeOrderRequireNew(tradeOrderReq,
				userOid);
		try {
			RedeemTradeOrderReq redeemReq = new RedeemTradeOrderReq();
			redeemReq.setCid(tradeOrderReq.getCid());
			redeemReq.setCkey(tradeOrderReq.getCkey());
			redeemReq.setOrderAmount(investOrder.getPayAmount());
			redeemReq.setUid(userOid);
			redeemReq.setProductOid(tradeOrderReq.getRedeemProductOid());
			/**  活期赎回  */
			InvestorTradeOrderEntity redeemOrder = this.investorRedeemTradeOrderService.createNoPayRedeemTradeOrder(redeemReq);
			investorRedeemTradeOrderService.redeemRequiresNew(redeemOrder.getOrderCode());
			/** 有奖励活期产品投资 */
			investOrder.setRelateOid(redeemOrder.getOid());
			InvestorTradeOrderEntity investOrderEntity = this.investorTradeOrderService.findByOrderCode(investOrder.getOrderCode());
			investorInvestTradeOrderService.invest(investOrderEntity);
			
			RedeemInvestParams params = new RedeemInvestParams();
			params.setInvestOrderCode(investOrder.getOrderCode());
			params.setRedeemOrderCode(redeemOrder.getOrderCode());

			SerialTaskReq<RedeemInvestParams> sreq = new SerialTaskReq<RedeemInvestParams>();
			sreq.setTaskParams(params);
			sreq.setTaskCode(SerialTaskEntity.TASK_taskCode_redeemInvest);
			serialTaskService.createSerialTask(sreq);
			tradeOrderRep.setTradeOrderOid(investOrder.getOid());
		} catch (Exception e) {
			log.error("活转活订单发生异常，订单号为{}", investOrder.getOrderCode(), e);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage(e.getMessage());
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			if (!(e instanceof AMPException) || !AMPException.CONCURRENT_CODE.equals(((AMPException)e).getCode())) {
				this.tulipService.sendInvestFailInNewTransaction(investOrder.getOrderCode());
			}
		}
		investorInvestTradeOrderService.investThen(tradeOrderRep, investOrder.getOrderCode());

		return tradeOrderRep;
	}


	private TradeOrderRep validateInvestProductAndCoupon(RedeemInvestTradeOrderReq tradeOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		
		/** 校验活期产品信息 */
		cacheProductService.isInDealTime(tradeOrderReq.getRedeemProductOid());
		/** 校验活期产品渠道 */
		this.cacheChannelService.checkChannel(tradeOrderReq.getCid(), tradeOrderReq.getCkey(), tradeOrderReq.getRedeemProductOid());
		
		/** 校验定期产品交易时间 */
		cacheProductService.isInDealTime(tradeOrderReq.getInvestProductOid());
		/** 校验定期产品渠道 */
		this.cacheChannelService.checkChannel(tradeOrderReq.getCid(), tradeOrderReq.getCkey(), tradeOrderReq.getInvestProductOid());
		return tradeOrderRep;
	}
	
	/**
	 * 活转定、定转活
	 */
	@Transactional
	public void redeemInvestDo(RedeemInvestParams params) {

		TradeOrderRep rep = new TradeOrderRep();
		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(params.getInvestOrderCode());
		try {
			investorRedeemInvestTradeOrderService.redeemInvestDo(params.getInvestOrderCode(), params.getRedeemOrderCode());
		} catch (Exception e) {
			log.error("活转定，活转活，定转活序列化任务发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage(e.getMessage());
			rep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			//活转定失败锁定份额做反向操作（加）--2017.12.29-----
			this.productService.updateProduct4LockCollectedVolume(orderEntity, true);
			//活转定失败锁定份额做反向操作（加）--2017.12.29-----
			this.tulipService.sendInvestFail(orderEntity);
			
		}
		investorInvestTradeOrderService.investThen(rep, orderEntity.getOrderCode());
		
	}


	@Transactional
	public TradeOrderRep writerOffOrder(TradeOrderReq tradeOrderReq) {
		
		TradeOrderRep tradeOrderRep = new TradeOrderRep();

		InvestorTradeOrderEntity orderEntity = investorInvestTradeOrderService.createWriteOffTradeOrder(tradeOrderReq);
		
		try {

			tradeOrderRep = investorInvestTradeOrderService.investRequireNew(orderEntity.getOrderCode());
		} catch (Exception e) {
			log.error("撤销赎回订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage(e.getMessage());
			if (!(e instanceof AMPException) || !AMPException.CONCURRENT_CODE.equals(((AMPException)e).getCode())) {
				this.tulipService.sendInvestFailInNewTransaction(orderEntity.getOrderCode());
			}
			
		}
		
		investorInvestTradeOrderService.investThen(tradeOrderRep, orderEntity.getOrderCode());

		return tradeOrderRep;
	}
	
	@Transactional
	public TradeOrderRep cashFailOrder(RedeemInvestTradeOrderReq riReq, String userOid) {
		TradeOrderRep rep = new TradeOrderRep();
		
		/** 创建订单 */
		RedeemTradeOrderReq redeemTradeOrderReq = new RedeemTradeOrderReq();
		redeemTradeOrderReq.setOrderAmount(riReq.getOrderAmount());
		redeemTradeOrderReq.setProductOid(riReq.getRedeemProductOid());
		redeemTradeOrderReq.setUid(userOid);
		InvestorTradeOrderEntity redeemOrder = this.investorRedeemTradeOrderService.createCashFailTradeOrder(redeemTradeOrderReq);
		try {
			
			rep = investorRedeemTradeOrderService.redeem(redeemOrder);
			
			InvestorTradeOrderEntity investOrder = investorInvestTradeOrderService.createNoPayInvestTradeOrder(riReq, userOid);
			investOrder.setRelateOid(redeemOrder.getOid());
			rep = investorInvestTradeOrderService.invest(investOrder);
			
			RedeemInvestParams params = new RedeemInvestParams();
			params.setInvestOrderCode(investOrder.getOrderCode());
			params.setRedeemOrderCode(redeemOrder.getOrderCode());
			
			SerialTaskReq<RedeemInvestParams> sreq = new SerialTaskReq<RedeemInvestParams>();
			sreq.setTaskParams(params);
			sreq.setTaskCode(SerialTaskEntity.TASK_taskCode_redeemInvest);
			serialTaskService.createSerialTask(sreq);
		} catch (Exception e) {
			log.error("流标订单发生异常，赎回订单号为{}", redeemOrder.getOrderCode(), e);
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage(e.getMessage());
			rep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			
		}
		
		return rep;
	}
	
	
	@Transactional
	public TradeOrderRep cashOrder(RedeemInvestTradeOrderReq riReq, String userOid) {
		TradeOrderRep rep = new TradeOrderRep();

		/** 创建订单 */
		RedeemTradeOrderReq redeemTradeOrderReq = new RedeemTradeOrderReq();
		redeemTradeOrderReq.setOrderAmount(riReq.getOrderAmount());
		redeemTradeOrderReq.setProductOid(riReq.getRedeemProductOid());
		redeemTradeOrderReq.setUid(userOid);
		InvestorTradeOrderEntity redeemOrder = this.investorRedeemTradeOrderService
				.createCashTradeOrder(redeemTradeOrderReq);
		rep = investorRedeemTradeOrderService.redeem(redeemOrder);
		InvestorTradeOrderEntity investOrder = investorInvestTradeOrderService.createNoPayInvestTradeOrder(riReq,
				userOid);
		investOrder.setRelateOid(redeemOrder.getOid());
		rep = investorInvestTradeOrderService.invest(investOrder);
		RedeemInvestParams params = new RedeemInvestParams();
		params.setInvestOrderCode(investOrder.getOrderCode());
		params.setRedeemOrderCode(redeemOrder.getOrderCode());

		SerialTaskReq<RedeemInvestParams> sreq = new SerialTaskReq<RedeemInvestParams>();
		sreq.setTaskParams(params);
		sreq.setTaskCode(SerialTaskEntity.TASK_taskCode_redeemInvest);
		serialTaskService.createSerialTask(sreq);
		return rep;
	}

    /**
     * 获取赎回页面相关数据
     * @param bfPlusReq
     * @return
     */
    public TradeOrderBFPlusRep bfPlusToRedeem(String uid, TradeOrderBFPlusReq bfPlusReq){
        InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(bfPlusReq.getOrderCode());
		TradeOrderBFPlusRep tradeOrderBFPlusRep = new TradeOrderBFPlusRep();
		if(orderEntity == null){
			tradeOrderBFPlusRep.setErrorCode(-1);
			tradeOrderBFPlusRep.setErrorMessage(bfPlusReq.getOrderCode() + ":此订单不存在");
			return tradeOrderBFPlusRep;
		}
        if(!InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed.equals(orderEntity.getOrderStatus())){
            tradeOrderBFPlusRep.setErrorCode(-1);
            tradeOrderBFPlusRep.setErrorMessage(bfPlusReq.getOrderCode() + ":此订单不可赎回！");
            return tradeOrderBFPlusRep;
        }
		Product product = productService.getProductByOid(orderEntity.getProduct().getOid());
		if(DateUtil.daysBetween(product.getDurationPeriodEndDate()) > 0){
			tradeOrderBFPlusRep.setErrorCode(2);
			tradeOrderBFPlusRep.setErrorMessage("存续期已过，无法赎回");
            return tradeOrderBFPlusRep;
		}
		if(!orderEntity.getInvestorBaseAccount().getOid().equals(uid)){
			tradeOrderBFPlusRep.setErrorCode(-1);
			tradeOrderBFPlusRep.setErrorMessage("订单投资人和操作人不是同一人！");
            return tradeOrderBFPlusRep;
		}
		/////////////
        if(!Product.TYPE_Producttype_04.equals(product.getType().getOid())){
            tradeOrderBFPlusRep.setErrorCode(-1);
            tradeOrderBFPlusRep.setErrorMessage("赎回订单所属产品类型错误！");
            return tradeOrderBFPlusRep;
        }
        /** 校验产品交易时间 */
        cacheProductService.isInDealTime(orderEntity.getProduct().getOid());
        /** 校验是否在停赎期间 */
        cacheProductService.isInStopRedeemTime();
        Date confirmDate = orderEntity.getConfirmDate();
        int durationPeriodDays = DateUtil.daysBetween(confirmDate);
        if(InvestorTradeOrderEntity.TRADEORDER_orderType_continueInvest.equals(orderEntity.getOrderType())//续投订单，续投赎回不出手续费
				|| (InvestorTradeOrderEntity.TRADEORDER_orderType_changeInvest.equals(orderEntity.getOrderType())
				&& (durationPeriodDays + 1) > orderEntity.getProduct().getMovedupRedeemLockDays())){
			tradeOrderBFPlusRep.setFree(true);

			tradeOrderBFPlusRep.setLimitAmount(orderEntity.getProduct().getMovedupRedeemMinPay());
			tradeOrderBFPlusRep.setToDate(tradeCalendarService.nextTrade(new java.sql.Date(Calendar.getInstance().getTimeInMillis()), 1));
			tradeOrderBFPlusRep.setApr(orderEntity.getProduct().getBasicRatio());
			tradeOrderBFPlusRep.setDurationPeriodDays(durationPeriodDays);
            tradeOrderBFPlusRep.setInvestMin(product.getInvestMin());
			tradeOrderBFPlusRep.setIncomeCalcBasis(product.getIncomeCalcBasis());

			return tradeOrderBFPlusRep;
		}

        // 5日内收取手续费
        tradeOrderBFPlusRep.setFree(false);
        tradeOrderBFPlusRep.setLimitAmount(orderEntity.getProduct().getMovedupRedeemMinPay());
        tradeOrderBFPlusRep.setRate(orderEntity.getProduct().getMovedupRedeemRate());
        tradeOrderBFPlusRep.setToDate(tradeCalendarService.nextTrade(new java.sql.Date(Calendar.getInstance().getTimeInMillis()), 1));
        tradeOrderBFPlusRep.setApr(orderEntity.getProduct().getBasicRatio());
		tradeOrderBFPlusRep.setDurationPeriodDays(durationPeriodDays);// 实际存续天数
		tradeOrderBFPlusRep.setInvestMin(product.getInvestMin());
        tradeOrderBFPlusRep.setMinimalFees(product.getMovedupRedeemMinPay());
        tradeOrderBFPlusRep.setIncomeCalcBasis(product.getIncomeCalcBasis());
        /////////////
        return tradeOrderBFPlusRep;
    }

	@Transactional
	public TradeOrderRep plusRedeem(PlusRedeemTradeOrderReq redeemTradeOrderReq) {
		String cycleprocuctname = DealMessageEnum.CYCLEPROCUCTNAME;
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
        Product product = productService.getProductByOid(redeemTradeOrderReq.getProductOid());
		if(product == null) {
			tradeOrderRep.setErrorCode(-1);
			tradeOrderRep.setErrorMessage("赎回的产品不存在");
			return tradeOrderRep;
		}
		if(!Product.TYPE_Producttype_04.equals(product.getType().getOid())) {
			tradeOrderRep.setErrorCode(-1);
			tradeOrderRep.setErrorMessage("赎回的产品必须是"+cycleprocuctname);
			return tradeOrderRep;
		}
		/** 校验产品交易时间 */
		cacheProductService.isInDealTime(redeemTradeOrderReq.getProductOid());
		/** 校验是否在停赎期间 */
		cacheProductService.isInStopRedeemTime();
		RedeemTradeOrderReq redeemTradeOrder = new RedeemTradeOrderReq();
		redeemTradeOrder.setUid(redeemTradeOrderReq.getUid());
		if(!validateBaseAccount(redeemTradeOrder, tradeOrderRep)) {
			return tradeOrderRep;
		}

		/** 创建订单 */
        PlusRedeemAfterReq plusRedeemAfterReq = this.investorRedeemTradeOrderService.createBfPlusRedeemTradeOrder(redeemTradeOrderReq);
		try {
			tradeOrderRep = investorRedeemTradeOrderService.plusRedeemRequiresNew(plusRedeemAfterReq);
		} catch (Exception e) {
			log.error("赎回订单发生异常，订单号为{}", plusRedeemAfterReq.getRedeemOrderEntity().getOrderCode(), e);
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			if (e instanceof GHException) {
				tradeOrderRep.setErrorMessage(e.getMessage());
			} else {
				tradeOrderRep.setErrorMessage("系统繁忙，请稍后重试！");
			}
			investorTradeOrderService.refuseOrder(plusRedeemAfterReq.getRedeemOrderEntity().getOrderCode());
		}

		investorRedeemTradeOrderService.redeemThen(tradeOrderRep, plusRedeemAfterReq.getRedeemOrderEntity().getOrderCode());
		return tradeOrderRep;
	}

	@Transactional
	public TradeOrderRep redeem(RedeemTradeOrderReq redeemTradeOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		List<ProductIncomeReward> rewardList = rewardCacheService.readCache(redeemTradeOrderReq.getProductOid());
		ProductCacheEntity productCacheEntity = cacheProductService.getProductCacheEntityById(redeemTradeOrderReq.getProductOid());
		if(productCacheEntity == null) {
			tradeOrderRep.setErrorCode(-1);
			tradeOrderRep.setErrorMessage("赎回的产品不存在");
			return tradeOrderRep;
		}
		if(!rewardList.isEmpty() || !Product.TYPE_Producttype_02.equals(productCacheEntity.getType())) {
			tradeOrderRep.setErrorCode(-1);
			tradeOrderRep.setErrorMessage("赎回的产品必须是快活宝");
			return tradeOrderRep;
		}
		/** 校验产品交易时间 */
		cacheProductService.isInDealTime(redeemTradeOrderReq.getProductOid());
		/** 校验是否在停赎期间 */
		cacheProductService.isInStopRedeemTime();
		if(!validateBaseAccount(redeemTradeOrderReq, tradeOrderRep)) {
			return tradeOrderRep;
		}
		/** 创建订单 */
		InvestorTradeOrderEntity orderEntity = this.investorRedeemTradeOrderService
				.createNormalRedeemTradeOrder(redeemTradeOrderReq);
		try {

			tradeOrderRep = investorRedeemTradeOrderService.redeemRequiresNew(orderEntity.getOrderCode());
		} catch (Exception e) {
			log.error("赎回订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			if (e instanceof GHException) {
				tradeOrderRep.setErrorMessage(e.getMessage());
			} else {
				tradeOrderRep.setErrorMessage("系统繁忙，请稍后重试！");
			}
			investorTradeOrderService.refuseOrder(orderEntity.getOrderCode());
		}

		investorRedeemTradeOrderService.redeemThen(tradeOrderRep, orderEntity.getOrderCode());
		//不是拒绝状态的订单发送消息
//		if(!InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused.equals(tradeOrderRep.getOrderStatus())){
//			sendMessage(orderEntity, DealMessageEnum.REDEEM_APPLY.name());
//		}
		return tradeOrderRep;
	}

	@Transactional
	public TradeOrderRep incrementRedeem(RedeemTradeOrderReq redeemTradeOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		List<ProductIncomeReward> rewardList = rewardCacheService.readCache(redeemTradeOrderReq.getProductOid());
		if(rewardList.isEmpty() || !Product.TYPE_Producttype_02.equals(rewardList.get(0).getProduct().getType().getOid())) {
			tradeOrderRep.setErrorCode(-1);
			tradeOrderRep.setErrorMessage("赎回的产品必须是天天向上");
			return tradeOrderRep;
		}
		/** 校验产品交易时间 */
		cacheProductService.isInDealTime(redeemTradeOrderReq.getProductOid());
		/** 校验是否在停赎期间 */
		cacheProductService.isInStopRedeemTime();
		if(!validateBaseAccount(redeemTradeOrderReq, tradeOrderRep)) {
			return tradeOrderRep;
		}
		/** 创建订单 */
		InvestorTradeOrderEntity orderEntity = this.investorRedeemTradeOrderService
				.createIncrementRedeemTradeOrder(redeemTradeOrderReq);
		
		try {
			
			tradeOrderRep = investorRedeemTradeOrderService.redeemRequiresNew(orderEntity.getOrderCode());
		} catch (Exception e) {
			log.error("天天向上赎回订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			if (e instanceof GHException) {
				tradeOrderRep.setErrorMessage(e.getMessage());
			} else {
				tradeOrderRep.setErrorMessage("系统繁忙，请稍后重试");
			}
			investorTradeOrderService.refuseOrder(orderEntity.getOrderCode());
		}
		investorRedeemTradeOrderService.redeemThen(tradeOrderRep, orderEntity.getOrderCode());
		//不是拒绝状态的订单发送消息
//		if(!InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused.equals(tradeOrderRep.getOrderStatus())){
//			sendMessage(orderEntity, DealMessageEnum.REDEEM_APPLY.name());
//		}
		return tradeOrderRep;
	}
	
	private boolean validateBaseAccount(RedeemTradeOrderReq redeemTradeOrderReq, TradeOrderRep tradeOrderRep) {
		InvestorBaseAccountEntity baseAccount = investorBaseAccountService.findByUid(redeemTradeOrderReq.getUid());
		if(baseAccount == null || "forbidden".equals(baseAccount.getStatus())) {
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage("当前用户被锁定，无法提现");
			return false;
		} else if("writeOff".equals(baseAccount.getStatus())) {
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage("当前用户已冻结，无法提现");
			return false;
		}
		return true;
	}
	
	private void sendMessage(InvestorTradeOrderEntity orderEntity, String tag) {
		DealMessageEntity messageEntity = new DealMessageEntity();
		messageEntity.setPhone(orderEntity.getInvestorBaseAccount().getPhoneNum());
		messageEntity.setUserName(orderEntity.getInvestorBaseAccount().getRealName());
		messageEntity.setOrderAmount(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
		messageEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem);
		messageEntity.setOrderTime(orderEntity.getOrderTime());
		messageEntity.setProductName(orderEntity.getProduct().getName());
		messageEntity.setUserOid(orderEntity.getInvestorBaseAccount().getOid());
		messageEntity.setOrderCode(orderEntity.getOrderCode());
		messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
	}
	
	@Transactional
	public void redeemDo(String orderCode, String taskOid) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		try {
			tradeOrderRep = investorRedeemTradeOrderService.redeemDoRequireNew(orderCode);
		} catch (Exception e) {
			log.error("赎回订单序列化任务发生异常，赎回订单号为{}", orderCode, e);
			// 如果异常是自定义异常说明校验未通过，将提现单用户数据库持有份额回滚
			if (e instanceof AMPException) {
				this.publisherHoldService.normalRedeemFailed(orderCode);
			}
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage(AMPException.getStacktrace(e));
			investorTradeOrderService.refuseOrder(orderCode);
		}
		// 赎回日志记录
		investorRedeemTradeOrderService.redeemThen(tradeOrderRep, orderCode);
		serialTaskRequireNewService.updateTime(taskOid);
	}
	
	@Transactional
	public TradeOrderRep specialRedeem(SpecialRedeemOrderReq specialredeemOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		
		InvestorBaseAccountEntity baseAccount = investorBaseAccountService.findByUid(specialredeemOrderReq.getUid());
		Product product = this.productService.findByOid(specialredeemOrderReq.getProductOid());
		if("writeOff".equals(baseAccount.getStatus())) {
			InvestorSpecialRedeemAuthEntity specialRedeemAuth =	investorSpecialRedeemAuthService.findByUserIdOperateStatus(specialredeemOrderReq.getUid(), "toOperate");
			Date now = new Date();
			// 判断用户的持有金额与赎回订单的金额
			PublisherHoldEntity publisherHold = this.publisherHoldService.findByInvestorBaseAccountAndProduct(baseAccount, product);
			if(product.getNetUnitShare().multiply(publisherHold.getRedeemableHoldVolume()).compareTo(specialredeemOrderReq.getOrderAmount()) < 0){
				tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
				tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
				tradeOrderRep.setErrorMessage("订单金额不能大于当前用户持有金额!");
				return tradeOrderRep;
			}
			
			// 判断这个用户是或否有未完成的特殊赎回订单，如果有，则不允许创建新的特殊赎回订单
			List<InvestorTradeOrderEntity> specialRedeemOrderList = this.investorTradeOrderService.findSpecialRedeemOrder(specialredeemOrderReq.getUid());
			if(specialRedeemOrderList.size()>0){
				tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
				tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
				tradeOrderRep.setErrorMessage("当前用户存在未处理完成的特殊赎回订单!");
				return tradeOrderRep;
			}
			
			// 判断创建订单金额是否等于初始化表t_money_investor_specialredeem_auth中的授权金额，否则无法创建订单
			if(specialRedeemAuth.getAuthAmount().compareTo(specialredeemOrderReq.getOrderAmount())!=0){
				tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
				tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
				tradeOrderRep.setErrorMessage("当前订单金额与授权金额不相等!");
				return tradeOrderRep;
			}
			
			// 判断当前时间是否在授权开始结束时间之内，否则不能创建订单
			if(specialRedeemAuth.getStartTime().getTime() >= now.getTime() || specialRedeemAuth.getEndTime().getTime() <= now.getTime()){
				tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
				tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
				tradeOrderRep.setErrorMessage("当前用户可用份额不在有效期内!");
				return tradeOrderRep;
			}
			// 乐观锁，将授权订单状态更新为已操作
			this.investorSpecialRedeemAuthService.updateOperateStatus(specialredeemOrderReq.getUid(), "operated");
			/** 创建订单 */
			InvestorTradeOrderEntity orderEntity = this.investorRedeemTradeOrderService
					.createSpecialRedeemTradeOrder(specialredeemOrderReq);
		
			try {
				tradeOrderRep = investorRedeemTradeOrderService.redeemRequiresNew(orderEntity.getOrderCode());
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
				tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
				tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
				tradeOrderRep.setErrorMessage(e.getMessage());
				investorTradeOrderService.refuseOrder(orderEntity.getOrderCode());
			}
			investorRedeemTradeOrderService.redeemThen(tradeOrderRep, orderEntity.getOrderCode());
			return tradeOrderRep;
		}else{
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage("用户账号状态不正确！");
			return tradeOrderRep;
		}
	}
	
	/**
	 * 投资回调
	 */
	@Transactional
	public boolean investCallBack(OrderNotifyReq ireq) {
		boolean flag = true;
		try {
			InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(ireq.getOrderCode());
			
			if (PayParam.ReturnCode.RC0000.toString().equals(ireq.getReturnCode())) {
				log.info("<----------投资回调成功------------>");
				
				/**如果是自动扣款**/
				if(orderEntity.getIsAuto().equals(FamilyEnum.isAuto0.getCode())){
					this.familyInvestPlanService.paySuccess(orderEntity.getInvestorBaseAccount().getOid());
				}
				/** 更新订单状态 */
				orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_paySuccess);
				orderEntity.setPayChannel(ireq.getPayChannel());
				this.investorTradeOrderService.saveEntity(orderEntity);

				//03产品目前不参与活动
				String productType= Optional.ofNullable(orderEntity.getProduct()).map(Product::getType).map(Dict::getOid).orElse("");

				// 获取用户投资数据转存至用户累计投资记录中
				//如果属于用户银行卡投资类型处理，其他不处理
				if (!Objects.equals(Product.TYPE_Producttype_03,productType)&&Objects.equals(InvestorTradeOrderEntity.TRADEORDER_orderType_invest, orderEntity.getOrderType())) {
					DealMessageEntity messageEntity_trade_statistics=new DealMessageEntity();
					messageEntity_trade_statistics.setTradeorder_stastistics_userOid(orderEntity.getInvestorBaseAccount().getOid());
					messageEntity_trade_statistics.setTradeorder_stastistics_phoneNum(orderEntity.getInvestorBaseAccount().getPhoneNum());
					messageEntity_trade_statistics.setTradeorder_stastistics_payAmount(orderEntity.getPayAmount());
					messageEntity_trade_statistics.setTradeorder_stastistics_orderTime(orderEntity.getCreateTime().getTime());
					messageEntity_trade_statistics.setTradeorder_stastistics_orderCode(orderEntity.getOrderCode());
					messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), MessageConstant.MESSAGE_QUEUE_DEAL_TRADEORDER_STATISTICS_TAG, messageEntity_trade_statistics);
					
					/**
					 * 投资消息
					 * yujianlong
					 * 2017-11-22
					 */
					DealMessageEntity messageEntity = new DealMessageEntity();
					messageEntity.setTriggerUserOid(orderEntity.getInvestorBaseAccount().getOid());
					messageEntity.setTriggerProductOid(orderEntity.getProduct().getOid());
					messageEntity.setPhone(orderEntity.getInvestorBaseAccount().getPhoneNum());
					messageEntity.setUserName(orderEntity.getInvestorBaseAccount().getRealName());
					messageEntity.setTriggerOrderCode(orderEntity.getOrderCode());
					messageEntity.setTriggerOrderAmount(orderEntity.getOrderAmount());
					messageEntity.setTriggerIsAuto(orderEntity.getIsAuto());
					messageEntity.setTriggerDurationPeriodDays(orderEntity.getProduct().getDurationPeriodDays());
					messageEntity.setTriggerOrderType(orderEntity.getOrderType());
					messageEntity.setTriggerOrderStatus(orderEntity.getOrderStatus());
					messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), MessageConstant.MESSAGE_QUEUE_DEAL_ACTIVITY_TAG, messageEntity);
					
					/**
					 * 投资消息
					 * huyong
					 * 2018-01-09
					 */
					DealMessageEntity message = new DealMessageEntity();
					message.setOrderCode(orderEntity.getOrderCode());
					message.setUserOid(orderEntity.getInvestorBaseAccount().getOid());
					message.setTriggerProductOid(orderEntity.getProduct().getOid());
					message.setTradeorder_stastistics_payAmount(orderEntity.getPayAmount());
					message.setTriggerOrderType(orderEntity.getOrderType());
					message.setTradeorder_stastistics_orderTime(orderEntity.getCreateTime().getTime());
					messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), MessageConstant.MESSAGE_QUEUE_DEAL_ACTIVITY_COUPON_TAG, message);
				}

				InvestNotifyParams params = new InvestNotifyParams();
				params.setOrderCode(ireq.getOrderCode());
				params.setReturnCode(ireq.getReturnCode());
				SerialTaskReq<InvestNotifyParams> req = new SerialTaskReq<InvestNotifyParams>();
				req.setTaskParams(params);
				req.setTaskCode(SerialTaskEntity.TASK_taskCode_invest);
				serialTaskService.createSerialTask(req);
				log.info("<--------------投资回调序列化任务创建成功------------------------->");
			} else {
				if(orderEntity.getIsAuto().equals(FamilyEnum.isAuto0.getCode())){
					log.info("<--------------该订单为自动扣款订单------------------------->");
					this.familyInvestPlanService.payFail(orderEntity.getInvestorBaseAccount().getOid(),ireq.getErrorMessage());
				}
				log.info("<----------投资回调失败------------>");
//				String productType=Optional.ofNullable(orderEntity.getProduct()).map(Product::getType).map(Dict::getOid).orElse("");
				//修改申购缓存锁定份额被在途用户刷没的bug----回调失败锁定份额做反向操作（加）--2017.05.15-----
				this.productService.updateProduct4LockCollectedVolume(orderEntity, true);
				//修改申购缓存锁定份额被在途用户刷没的bug----回调失败锁定份额做反向操作（加）--2017.05.15-----
//				if (!Objects.equals(Product.TYPE_Producttype_03,productType)){
//				}

				// 用户申购产品支付失败-回退缓存中的申购上限 2017.05.16 //
				String hkey = CacheKeyConstants.getAssetPoolPurchaseLimit(orderEntity.getProduct().getAssetPool().getOid());
				hkey = CacheKeyConstants.getInvestorPurchaseLimit(orderEntity.getInvestorBaseAccount().getOid(), orderEntity.getProduct().getAssetPool().getOid());
				this.redisExecuteLogExtService.hincrByBigDecimal(hkey, orderEntity.getProduct().getOid(), orderEntity.getOrderAmount().negate(), null);
				this.redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(),
						orderEntity.getProduct().getOid(), orderEntity.getProduct().getAssetPool().getOid());
				// 用户申购产品支付失败-回退缓存中的申购上限 2017.05.16 //
				
				/** 更新订单状态 */
				orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_payFailed);
				this.investorTradeOrderService.saveEntity(orderEntity);
				
				// 处理新手状态(查看用户有无成功的新手标产品订单，如果没有，则将用户账号状态更新为新手)
//				ProductCacheEntity cache = this.cacheProductService.getProductCacheEntityById(orderEntity.getProduct().getOid());
//				if (Product.TYPE_Producttype_01.equals(cache.getType()) && labelService.isProductLabelHasAppointLabel(cache.getProductLabel(), LabelEnum.newbie.toString())) {
				if(!investorTradeOrderService.hasInvestSuccessOrder(orderEntity.getInvestorBaseAccount().getOid())){
					log.info("==================开始回滚新手,订单:{}===========================",orderEntity);
					int i = this.investorBaseAccountDao.updateFreshman2Yes(orderEntity.getInvestorBaseAccount().getOid());
					if (i < 1) {
						log.error("===============回滚新手失败,订单:{}===========================",orderEntity);
						// error.define[15003]=回退用户账号为新手失败(CODE:15003)
						throw new AMPException(15003);
					}
					log.info("==================回滚新手成功,订单:{}===========================",orderEntity);
				}
//				}
				this.tulipService.sendInvestFail(orderEntity);
			}
		} catch (Exception e) {
			flag = false;
			log.error("投资回调发生异常，订单号为{}", ireq.getOrderCode(), e);
		}
		return flag;
	}
	
	@Transactional
	public void investCallBackDo(String orderCode, String returnCode, String taskOid) {
		OrderLogEntity orderLog = new OrderLogEntity();
		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(orderCode);

		if (PayParam.ReturnCode.RC0000.toString().equals(returnCode)) {
			try {
				investorInvestTradeOrderService.investCallBackRequireNew(orderCode);
				/**投资成功，发送推荐事件，给推荐人下发体验券**/
				String uid = orderEntity.getInvestorBaseAccount().getUserOid();  //投资人id
				log.info("投资回调，投资人uid：" + uid);
			} catch (Exception e) {
				log.error("投资序列化任务发生异常，订单号为{}", orderEntity.getOrderCode(), e);
				orderLog.setErrorCode(BaseRep.ERROR_CODE);
				orderLog.setErrorMessage(e.getMessage());
				redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), orderEntity.getProduct().getOid(), orderEntity.getProduct().getAssetPool().getOid());
				
				this.tulipService.sendInvestFail(orderEntity);
			}
		}

		orderLog.setOrderType(orderEntity.getOrderType());
		orderLog.setTradeOrderOid(orderEntity.getOrderCode());
		orderLog.setOrderStatus(orderEntity.getOrderStatus());
		this.orderLogService.create(orderLog);

		serialTaskRequireNewService.updateTime(taskOid);
	}

	

	
	@Transactional
	public BaseRep resumitInvestOrder(CheckOrderReq checkOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		modifyOrderNewService.updateDealStatusDealingByOrderCode(checkOrderReq.getOrderCode());
		
		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(checkOrderReq.getOrderCode());
		if (null != orderEntity) {
			String orderStatus = InvestorTradeOrderEntity.TRADEORDER_orderStatus_toPay;
			String payStatus = InvestorTradeOrderEntity.TRADEORDER_payStatus_toPay;
			investorTradeOrderService.saveEntityNewTrans(orderEntity, payStatus, orderStatus);
			
			InnerOrderRep irep = new InnerOrderRep();
			irep.setOrderNo(orderEntity.getOrderCode());
			irep.setReturnCode(PayParam.ReturnCode.RC0000.toString());
			//paymentServiceImpl.innerTradeCallback(irep);
			
		} else {
			/** 创建订单 */
			TradeOrderReq req = new TradeOrderReq();
			req.setUid(investorBaseAccountService.findByMemberId(checkOrderReq.getMemberId()).getUserOid());
			req.setMoneyVolume(checkOrderReq.getMoneyVolume());
			req.setProductOid(checkOrderReq.getProductOid());
			
			orderEntity = investorInvestTradeOrderService.createReInvestTradeOrder(req);
			try {
				tradeOrderRep = investorInvestTradeOrderService.investRequireNew(orderEntity.getOrderCode());
				this.abandonLogService.create(checkOrderReq.getOrderCode(), orderEntity.getOrderCode());
			} catch (Exception e) {
				log.error("resumitInvest订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
				tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
				tradeOrderRep.setErrorMessage(e.getMessage());
				tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
				if (!(e instanceof AMPException) || !AMPException.CONCURRENT_CODE.equals(((AMPException)e).getCode())) {
					this.tulipService.sendInvestFailInNewTransaction(orderEntity.getOrderCode());
				}
			}
			
			investorInvestTradeOrderService.investThen(tradeOrderRep, orderEntity.getOrderCode());
		}
		modifyOrderNewService.updateDealStatusDealtByOrderCode(checkOrderReq.getOrderCode());
		return tradeOrderRep;
	}

	
	@Transactional
	public BaseRep resumitRedeemOrder(CheckOrderReq checkOrderReq) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		
		modifyOrderNewService.updateDealStatusDealingByOrderCode(checkOrderReq.getOrderCode());
		
		RedeemTradeOrderReq req = new RedeemTradeOrderReq();
		req.setUid(investorBaseAccountService.findByMemberId(checkOrderReq.getMemberId()).getUserOid());
		req.setOrderAmount(checkOrderReq.getMoneyVolume());
		req.setProductOid(checkOrderReq.getProductOid());
		/** 创建订单 */
		InvestorTradeOrderEntity orderEntity = this.investorRedeemTradeOrderService.createReRedeemTradeOrder(req);
		try {
			
			tradeOrderRep = investorRedeemTradeOrderService.redeemRequiresNew(orderEntity.getOrderCode());
			this.abandonLogService.create(checkOrderReq.getOrderCode(), orderEntity.getOrderCode());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage(AMPException.getStacktrace(e));
			investorTradeOrderService.refuseOrder(orderEntity.getOrderCode());
		}
		// 赎回日志记录
		investorRedeemTradeOrderService.redeemThen(tradeOrderRep, orderEntity.getOrderCode());
		modifyOrderNewService.updateDealStatusDealtByOrderCode(checkOrderReq.getOrderCode());
		return tradeOrderRep;
	}
	/**
	 * 家庭理财计划自动申购单
	 * @param tradeOrderReq
	 * @return
	 */
	@Transactional
	public TradeOrderRep autoInvest(TradeOrderReq tradeOrderReq) {
		
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		/** 校验产品交易时间 */
		cacheProductService.isInDealTime(tradeOrderReq.getProductOid());
		InvestorTradeOrderEntity orderEntity = investorInvestTradeOrderService.createAutoTradeOrder(tradeOrderReq);
		
		try {
			tradeOrderRep = investorInvestTradeOrderService.investRequireNew(orderEntity.getOrderCode());
		} catch (Exception e) {
			log.error("家庭理财自动申购订单发生异常，订单号为{}", orderEntity.getOrderCode(), e);
			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
			tradeOrderRep.setErrorMessage(e.getMessage());
			if (!(e instanceof AMPException) || !AMPException.CONCURRENT_CODE.equals(((AMPException)e).getCode())) {
				this.tulipService.sendInvestFailInNewTransaction(orderEntity.getOrderCode());
			}
		}
		investorInvestTradeOrderService.investThen(tradeOrderRep, orderEntity.getOrderCode());
		return tradeOrderRep;
	}
	
}
