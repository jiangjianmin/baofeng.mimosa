package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.ams.channel.ChannelService;
import com.guohuai.ams.dict.Dict;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.coupon.ProductCouponEnum;
import com.guohuai.ams.product.coupon.ProductCouponService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.cache.CacheKeyConstants;
import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.cache.service.CacheSPVHoldService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.Clock;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.version.VersionUtils;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.cashflow.InvestorCashFlowService;
import com.guohuai.mmp.investor.coupon.InvestorCouponOrderEntity;
import com.guohuai.mmp.investor.orderlog.OrderLogEntity;
import com.guohuai.mmp.investor.orderlog.OrderLogService;
import com.guohuai.mmp.platform.accment.AccParam;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.accment.TradeRequest;
import com.guohuai.mmp.platform.payment.PayParam;
import com.guohuai.mmp.platform.payment.PayRequest;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;
import com.guohuai.mmp.platform.publisher.offset.OffsetService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.platform.redis.RedisSyncService;
import com.guohuai.mmp.platform.tulip.TulipService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.investor.InterestFormula;
import com.guohuai.mmp.serialtask.InvestNotifyParams;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskService;
import com.guohuai.mmp.sys.CodeConstants;
import com.guohuai.mmp.tulip.sdk.TulipSDKService;
import com.guohuai.moonBox.FamilyEnum;
import com.guohuai.moonBox.service.FamilyInvestPlanService;
import com.guohuai.usercenter.api.UserCenterSdk;
import com.guohuai.usercenter.api.obj.RecommenderReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.guohuai.ams.duration.capital.calc.error.ErrorCalc_.message;

@Service
@Transactional
public class InvestorInvestTradeOrderService  {
	
	
	Logger logger = LoggerFactory.getLogger(InvestorInvestTradeOrderService.class);
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private InvestorOpenCycleService investorOpenCycleService;
	@Autowired
	private ProductService productService;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private InvestorCashFlowService investorCashFlowService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private ProductOffsetService productOffsetService;
	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	@Autowired
	private TulipService tulipService;
	@Autowired
	private TulipSDKService tulipSDKService;
	@Autowired
	private OrderLogService orderLogService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private CacheSPVHoldService cacheSPVHoldService;
	@Autowired
	private OffsetService offsetService;
	@Autowired
	private CacheHoldService cacheHoldService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private OrderDateService orderDateService;
	@Autowired
	private AccmentService accmentService;
	@Autowired
	private FamilyInvestPlanService familyInvestPlanService;
	@Autowired
	private MessageSendUtil messageSendUtil;
	@Autowired
	private UserCenterSdk userCenterSdk;
	@Autowired
	private LabelService labelService;
	@Autowired
	private RedisSyncService redisSyncService; 
	@Autowired
	private ProductCouponService productCouponService;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	/*募集满额*/
	public static final String RAISEFULLAMOUNT = "RAISEFULLAMOUNT";
	
	/** 记录投资订单日志 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void investThen(TradeOrderRep tradeOrderRep,
			String orderCode) {
		
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		/** 订单失败 */
		if (0 != tradeOrderRep.getErrorCode()){
			orderEntity.setOrderStatus(tradeOrderRep.getOrderStatus());
			investorTradeOrderService.saveEntity(orderEntity);
			// 活转定订单失败后将赎回订单状态同样置为refused  20171017 by wangpeng
			if (redisTemplate.hasKey(CacheKeyConstants.getRedeemInvestOrder(orderCode))) {
				String redeemOrderCode = redisTemplate.opsForValue().get(CacheKeyConstants.getRedeemInvestOrder(orderCode));
				InvestorTradeOrderEntity redeemOrderEntity = investorTradeOrderService.findByOrderCode(redeemOrderCode);
				redeemOrderEntity.setOrderStatus(tradeOrderRep.getOrderStatus());
				investorTradeOrderService.saveEntity(redeemOrderEntity);
				redisTemplate.delete(CacheKeyConstants.getRedeemInvestOrder(orderCode));
			}
			if(orderEntity.getIsAuto().equals(FamilyEnum.isAuto0.getCode())){
				logger.info("<--------------该订单为自动扣款订单------------------------->");
				this.familyInvestPlanService.payFail(orderEntity.getInvestorBaseAccount().getOid(), tradeOrderRep.getErrorMessage());
			}
			redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), orderEntity.getProduct().getOid(), orderEntity.getProduct().getAssetPool().getOid());
		}
		
		OrderLogEntity orderLog = new OrderLogEntity();
		orderLog.setErrorCode(tradeOrderRep.getErrorCode());
		orderLog.setErrorMessage(tradeOrderRep.getErrorMessage());
		orderLog.setOrderType(orderEntity.getOrderType());
		orderLog.setTradeOrderOid(orderEntity.getOrderCode());
		orderLog.setOrderStatus(orderEntity.getOrderStatus());
		this.orderLogService.create(orderLog);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public TradeOrderRep investRequireNew(String orderCode) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		return this.invest(orderEntity);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public TradeOrderRep investRequireNew(String orderCode, BigDecimal ratio, Integer raiseDays) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		orderEntity.setRatio(ratio);
		orderEntity.setRaiseDays(raiseDays);
		return this.invest(orderEntity);
	}


	@Transactional(value = TxType.REQUIRES_NEW)
	public TradeOrderGatewayRep investRequireNewThroughGateway(String orderCode, TradeOrderGatewayReq tradeOrderGatewayReq) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		orderEntity.setRatio(tradeOrderGatewayReq.getRatio());
		orderEntity.setRaiseDays(tradeOrderGatewayReq.getRaiseDays());
		return this.investThroughMode(orderEntity, tradeOrderGatewayReq.getPaymentMode(), tradeOrderGatewayReq.getBankCode());
	}

	public TradeOrderGatewayRep investThroughMode(InvestorTradeOrderEntity orderEntity, String paymentMode, String bankCode) {
		TradeOrderGatewayRep rep = new TradeOrderGatewayRep();

		/** 校验 */
		verification(orderEntity);

		/** 锁定卡券 */
		tulipService.lockCoupon(orderEntity);

		rep = payThroughMode(orderEntity, paymentMode, bankCode);

		serialSelfNotify(orderEntity);

		rep.setOrderStatus(orderEntity.getOrderStatus());
		rep.setTradeOrderOid(orderEntity.getOid());
		return rep;
	}
	
	public TradeOrderRep invest(InvestorTradeOrderEntity orderEntity) {
		TradeOrderRep rep = new TradeOrderRep();

		/** 校验 */
		verification(orderEntity);
		String productType=Optional.ofNullable(orderEntity).map(InvestorTradeOrderEntity::getProduct).map(Product::getType)
				.map(Dict::getOid).orElse("");
		if(!Objects.equals(Product.TYPE_Producttype_03,productType)){

			/** 锁定卡券 */
			tulipService.lockCoupon(orderEntity);
		}

		pay(orderEntity);
		
		serialSelfNotify(orderEntity);
		
		rep.setOrderStatus(orderEntity.getOrderStatus());
		rep.setTradeOrderOid(orderEntity.getOid());
		return rep;
	}


	private void serialSelfNotify(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(orderEntity.getOrderType())) {
			InvestNotifyParams params = new InvestNotifyParams();
			params.setOrderCode(orderEntity.getOrderCode());
			params.setReturnCode(PayParam.ReturnCode.RC0000.toString());
			SerialTaskReq<InvestNotifyParams> req = new SerialTaskReq<InvestNotifyParams>();
			req.setTaskParams(params);
			req.setTaskCode(SerialTaskEntity.TASK_taskCode_invest);
			serialTaskService.createSerialTask(req);
		}
	}


	private void pay(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
			PayRequest ireq = new PayRequest();
			ireq.setAmount(orderEntity.getPayAmount());
			ireq.setOrderNo(orderEntity.getOrderCode());
			ireq.setUserOid(orderEntity.getInvestorBaseAccount().getMemberId());
			ireq.setOrderTime(DateUtil.format(orderEntity.getOrderTime(), DateUtil.fullDatePattern));
			
			BaseRep baseRep = this.paymentServiceImpl.investPay(ireq);

			if (0 == baseRep.getErrorCode()) {
				orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_toPay);
				investorTradeOrderService.saveEntity(orderEntity);
			} else {
				//修改申购缓存锁定份额被在途用户刷没的bug----支付失败锁定份额做反向操作（加）--2017.05.15-----
				this.productService.updateProduct4LockCollectedVolume(orderEntity, true);
				//修改申购缓存锁定份额被在途用户刷没的bug----支付失败锁定份额做反向操作（加）--2017.05.15-----
				throw new AMPException(baseRep.getErrorMessage());
			}
			
		} else {
//          todo:需要了解改成topay的原因
//			orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_paySuccess);
			orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_toPay);
			investorTradeOrderService.saveEntity(orderEntity);
		}
	}

	private TradeOrderGatewayRep payThroughMode(InvestorTradeOrderEntity orderEntity, String paymentMode, String bankCode) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
			PayRequest ireq = new PayRequest();
			ireq.setAmount(orderEntity.getPayAmount());
			ireq.setOrderNo(orderEntity.getOrderCode());
			ireq.setUserOid(orderEntity.getInvestorBaseAccount().getMemberId());
			ireq.setOrderTime(DateUtil.format(orderEntity.getOrderTime(), DateUtil.fullDatePattern));
			ireq.setPaymentMode(paymentMode);
			ireq.setBankCode(bankCode);

			TradeOrderGatewayRep rep = this.paymentServiceImpl.investPayThroughMode(ireq);

			if (0 == rep.getErrorCode()) {
				orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_toPay);
				investorTradeOrderService.saveEntity(orderEntity);
			} else {
				//修改申购缓存锁定份额被在途用户刷没的bug----支付失败锁定份额做反向操作（加）--2017.05.15-----
				this.productService.updateProduct4LockCollectedVolume(orderEntity, true);
				//修改申购缓存锁定份额被在途用户刷没的bug----支付失败锁定份额做反向操作（加）--2017.05.15-----
				throw new AMPException(rep.getErrorMessage());
			}

			return rep;

		} else {
//          todo:需要了解改成topay的原因
//			orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_paySuccess);
			orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_toPay);
			investorTradeOrderService.saveEntity(orderEntity);
			return new TradeOrderGatewayRep();
		}
	}

	/**
	 * 校验
	 */
	public void verification(InvestorTradeOrderEntity orderEntity) {
		
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType()) || InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())) {
			/** 校验<<产品>> */
			this.cacheProductService.checkProduct4Invest(orderEntity);
			
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				/** 新手标检验 */
				this.investorBaseAccountService.isNewBie(orderEntity);
			}
		}
		String productType=Optional.ofNullable(orderEntity).map(InvestorTradeOrderEntity::getProduct).map(Product::getType)
				.map(Dict::getOid).orElse("");
		/** 产品可售份额 */
		this.cacheProductService.updateProduct4LockCollectedVolume(orderEntity);
		if(!Objects.equals(Product.TYPE_Producttype_03,productType)){
			/** 校验SPV持仓 */
			this.cacheSPVHoldService.checkSpvHold4Invest(orderEntity);

			/** 校验资产池持仓 */
			cacheHoldService.checkAssetPoolPurchaseLimit(orderEntity);

			/** 校验产品最大持仓 */
			cacheHoldService.checkMaxHold4Invest(orderEntity);
		}

		
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public void investCallBackRequireNew(String orderCode) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		this.investCallBack(orderEntity);
	}
	
	
	/**
	 * 更新支付状态
	 */
	public InvestorTradeOrderEntity investCallBack(InvestorTradeOrderEntity orderEntity) {
		
		vericationdb(orderEntity);
		
		/** 记录资金明细 */
		cashFlow(orderEntity);
		
		/** 参与轧差 */
		offset(orderEntity);
		
		/** 入仓 */
		writeHold(orderEntity);
		
		/** 确认 */
		confirm(orderEntity);

		/** 账户系统事件发送 */
		setAccSysInvestEvent(orderEntity);
		
		// 为了跨服务查询，需要flush
		this.investorTradeOrderService.saveAndFlush(orderEntity);
		
		// 保存成功再发券
		logger.info("========投资回调，tradeorder:{}=======", orderEntity);
		tulipService.sendInvestOK(orderEntity);
		
		sendMessage(orderEntity);
		return orderEntity;
	}
	
	private void sendMessage(InvestorTradeOrderEntity orderEntity) {
		String tag = "";
		DealMessageEntity messageEntity = new DealMessageEntity();
		messageEntity.setPhone(orderEntity.getInvestorBaseAccount().getPhoneNum());
		messageEntity.setOrderAmount(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
		messageEntity.setOrderTime(orderEntity.getOrderTime());
		messageEntity.setProductName(orderEntity.getProduct().getName());
		messageEntity.setUserOid(orderEntity.getInvestorBaseAccount().getOid());
		messageEntity.setUserName(orderEntity.getInvestorBaseAccount().getRealName());
		messageEntity.setOrderCode(orderEntity.getOrderCode());
		
		messageEntity.setOrderType(orderEntity.getOrderType());
		messageEntity.setProductType(orderEntity.getProduct().getType().getOid().toString());
		RecommenderReq recommenderReq = new RecommenderReq();
		recommenderReq.setUserId(orderEntity.getInvestorBaseAccount().getOid());
		String[] recommender = userCenterSdk.findRecommender(recommenderReq);
		if(recommender == null || recommender.length == 0){
			messageEntity.setReferOid("");
			messageEntity.setReferUserName("");
			messageEntity.setReferPhone("");
		}else{
			String referOid = recommender[0];
			InvestorBaseAccountEntity investorBaseAccount = investorBaseAccountService.findOne(referOid);
			messageEntity.setReferOid(referOid);
			messageEntity.setReferUserName(investorBaseAccount.getRealName());
			messageEntity.setReferPhone(investorBaseAccount.getPhoneNum());
		}
		String productType= Optional.ofNullable(orderEntity.getProduct()).map(Product::getType).map(Dict::getOid).orElse("");
		//判断是否企业散标
		String ISP2PASSETPACKAGE= Optional.ofNullable(orderEntity.getProduct()).map(Product::getIsP2PAssetPackage).map(Objects::toString).orElse("0");
		//如果是企业散标，发送募集满额判断的消息
		boolean isInvest = Stream.of(InvestorTradeOrderEntity.TRADEORDER_orderType_invest, InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest).anyMatch(t -> Objects.equals(t, orderEntity.getOrderType()));
		if (!Objects.equals(Product.TYPE_Producttype_03,productType) &&Objects.equals(Product.IS_P2P_ASSET_PACKAGE_2,ISP2PASSETPACKAGE)&&isInvest) {
			Product product1 = orderEntity.getProduct();
			BigDecimal raisedTotalNumber = product1.getRaisedTotalNumber();
			BigDecimal collectedVolume = product1.getCollectedVolume();
			if (raisedTotalNumber.doubleValue()>0&&collectedVolume.doubleValue()>0&&raisedTotalNumber.compareTo(collectedVolume)==0){
				DealMessageEntity message = new DealMessageEntity();
				message.setTriggerProductOid(orderEntity.getProduct().getOid());
				message.setTriggerOrderAmount(orderEntity.getOrderAmount());
				message.setOrderTime(new Date());
				message.setOrderCode(orderEntity.getOrderCode());
				messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), RAISEFULLAMOUNT, message);
			}
		}

		// 获取用户投资数据转存至用户累计投资记录中
		//如果属于用户银行卡投资类型处理，其他不处理
		boolean needSend = false;
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
			needSend = true;
			tag = DealMessageEnum.INVEST_DONE.name();
			if (Objects.equals(Product.TYPE_Producttype_03,productType)){
				tag = DealMessageEnum.CYCLE_INVEST_DONE.name();
			}
		} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())) {
			if (Product.TYPE_Producttype_01.equals(orderEntity.getProduct().getType().getOid())) {
				// 活转定
				needSend = true;
				tag = DealMessageEnum.TRANSFER_DONE.name();
			}
			if (Objects.equals(Product.TYPE_Producttype_03,productType)){
				needSend = true;
				tag = DealMessageEnum.CYCLE_INVEST_DONE.name();
			}
		} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(orderEntity.getOrderType())) {
			// 二级邀请奖励到账投资活期成功
			needSend = true;
			tag = DealMessageEnum.PROFIT_INVEST_DONE.name();
		}
		if (needSend) {
			messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
		}
	}

	/**
	 * 发送事件到账户系统
	 */
	private void setAccSysInvestEvent(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
			TradeRequest tradeRequest = new TradeRequest();
			tradeRequest.setOrderType(AccParam.OrderType.INVEST.toString());
			tradeRequest.setBalance(orderEntity.getOrderAmount());
			tradeRequest.setRelationProductNo(orderEntity.getProduct().getOid());
			tradeRequest.setUserOid(orderEntity.getInvestorBaseAccount().getMemberId());
			tradeRequest.setUserType(AccParam.UserType.INVESTOR.toString());
			tradeRequest.setOrderNo(orderEntity.getOrderCode());
			tradeRequest.setRemark("invest");
			accmentService.writeLog(tradeRequest);
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())) {
			TradeRequest tradeRequest = new TradeRequest();
			tradeRequest.setOrderType(AccParam.OrderType.DONATEEXP.toString());
			tradeRequest.setBalance(orderEntity.getOrderAmount());
			tradeRequest.setRelationProductNo(orderEntity.getProduct().getOid());
			tradeRequest.setUserOid(orderEntity.getInvestorBaseAccount().getMemberId());
			tradeRequest.setUserType(AccParam.UserType.INVESTOR.toString());
			tradeRequest.setOrderNo(orderEntity.getOrderCode());
			tradeRequest.setRemark("tasteCoupon invest");
			accmentService.writeLog(tradeRequest);
		}
		
	}
	

	public void vericationdb(InvestorTradeOrderEntity orderEntity) {
		//修改申购缓存锁定份额被在途用户刷没的bug------2017.05.15-----
//		/** 产品可售份额 */
//		this.productService.updateProduct4LockCollectedVolume(orderEntity);
		//修改申购缓存锁定份额被在途用户刷没的bug------2017.05.15-----
		/** 校验SPV持仓 */
		this.publisherHoldService.checkSpvHold4Invest(orderEntity);
		/** 校验用户最大持仓 */
		this.publisherHoldService.checkMaxHold4Invest(orderEntity);
	}

	private void cashFlow(InvestorTradeOrderEntity orderEntity) {
		/** 创建<<投资人-资金变动明细>> */
		investorCashFlowService.createCashFlow(orderEntity);

	}

	private InvestorTradeOrderEntity offset(InvestorTradeOrderEntity orderEntity) {
		//todo：需要了解现在不处理accept status和pay status会影响哪些功能
		/** 参与轧差 */
		String productType = Optional.ofNullable(orderEntity.getProduct()).map(Product::getType).map(Dict::getOid).orElse("");
		//判断是否是购买快定宝 快定宝要参与轧差
//		boolean isTypeMatch=Objects.equals(productType,Product.TYPE_Producttype_02)||Objects.equals(productType,Product.TYPE_Producttype_03);
		//03产品和
		if ((InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())
				&& Objects.equals(productType,Product.TYPE_Producttype_02))||Objects.equals(productType,Product.TYPE_Producttype_03)) {
			orderEntity.setPublisherOffset(this.publisherOffsetService.getLatestOffset(orderEntity,
					this.orderDateService.getConfirmDate(orderEntity)));
			productOffsetService.offset(orderEntity.getPublisherBaseAccount(), orderEntity, true);
			orderEntity.setPublisherClearStatus(InvestorTradeOrderEntity.TRADEORDER_publisherClearStatus_toClear);
			orderEntity.setPublisherConfirmStatus(InvestorTradeOrderEntity.TRADEORDER_publisherConfirmStatus_toConfirm);
			orderEntity.setPublisherCloseStatus(InvestorTradeOrderEntity.TRADEORDER_publisherCloseStatus_toClose);
		}
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_accepted);
		return orderEntity;
	}

	/**
	 * 确认份额
	 */
	private void writeHold(InvestorTradeOrderEntity orderEntity) {

		// 分仓
		BigDecimal expectIncome = getExpectIncome(orderEntity);
		BigDecimal expectIncomeExt = getExpectIncomeExt(orderEntity);
		
		/** 创建或更新 <<发行人-持有人手册>> */
		
		orderEntity.setHoldVolume(orderEntity.getOrderVolume()); // 持有份额
		orderEntity.setValue(orderEntity.getOrderVolume());
		orderEntity.setBeginAccuralDate(this.orderDateService.getBeginAccuralDate(orderEntity));
		orderEntity.setBeginRedeemDate(this.orderDateService.getBeginRedeemDate(orderEntity));
		orderEntity.setCorpusAccrualEndDate(this.orderDateService.getCorpusAccrualEndDate(orderEntity));
		orderEntity.setRedeemStatus(InvestorTradeOrderEntity.TRADEORDER_redeemStatus_no);
		orderEntity.setAccrualStatus(InvestorTradeOrderEntity.TRADEORDER_accrualStatus_no);
		orderEntity.setHoldStatus(InvestorTradeOrderEntity.TRADEORDER_holdStatus_toConfirm); // 待确认状态
		orderEntity.setExpectIncome(expectIncome);
		orderEntity.setExpectIncomeExt(expectIncomeExt);
		
		PublisherHoldEntity hold = publisherHoldService.invest(orderEntity);
		orderEntity.setPublisherHold(hold); // 所属持有人手册
	}
	
	
	/**
	 * noPayInvest针对 定转活投资
	 */
	private void confirm(InvestorTradeOrderEntity orderEntity) {
		boolean flag=InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff.equals(orderEntity.getOrderType())
				|| Product.TYPE_Producttype_01.equals(orderEntity.getProduct().getType().getOid())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest.equals(orderEntity.getOrderType());

		if (flag&&!Product.TYPE_Producttype_03.equals(orderEntity.getProduct().getType().getOid())) {
			offsetService.processItem(orderEntity);
		}
	}
	
	private BigDecimal getExpectIncomeExt(InvestorTradeOrderEntity orderEntity) {
		BigDecimal expectIncomeExt = BigDecimal.ZERO;
		String productType=Optional.ofNullable(orderEntity).map(InvestorTradeOrderEntity::getProduct).map(Product::getType).map(Dict::getOid).orElse("");
		if (Stream.of(Product.TYPE_Producttype_01,Product.TYPE_Producttype_03).anyMatch(s->s.equals(productType))){
			if (null != orderEntity.getProduct().getExpArorSec()) {
				InterestFormula.simple(orderEntity.getOrderAmount(),
						orderEntity.getProduct().getExpArorSec(), orderEntity.getProduct().getIncomeCalcBasis(), orderEntity.getProduct().getDurationPeriodDays());
			}
		}
		return expectIncomeExt;
	}


	/**
	 * 预期收益 起始
	 */
	private BigDecimal getExpectIncome(InvestorTradeOrderEntity orderEntity) {
		BigDecimal expectIncome = BigDecimal.ZERO;
		String productType=Optional.ofNullable(orderEntity).map(InvestorTradeOrderEntity::getProduct).map(Product::getType).map(Dict::getOid).orElse("");
		if (Stream.of(Product.TYPE_Producttype_01,Product.TYPE_Producttype_03).anyMatch(s->s.equals(productType))){
			expectIncome = InterestFormula.simple(orderEntity.getOrderAmount(),
					orderEntity.getProduct().getExpAror(), orderEntity.getProduct().getIncomeCalcBasis(), orderEntity.getProduct().getDurationPeriodDays());
		}
		return expectIncome;
	}

	/**
	 * 补投资单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createReInvestTradeOrder(TradeOrderReq tradeOrderReq) {
		tradeOrderReq.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_reInvest);
		InvestorTradeOrderEntity orderEntity = createInvestTradeOrder(tradeOrderReq);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		orderEntity.setOrderTime(DateUtil.getReInvestOrderTime());
		return investorTradeOrderService.saveEntity(orderEntity);
	}
	
	/**
	 * 体验金投资
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createExpGoldInvestTradeOrder(TradeOrderReq tradeOrderReq) {
		tradeOrderReq.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest);
		InvestorTradeOrderEntity orderEntity = createInvestTradeOrder(tradeOrderReq);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		if (null != tradeOrderReq.getCid()) {// 无论渠道是否为空，均进行体验金发放，这个判读是为了避免渠道为空时体验金发放不了
			orderEntity.setChannel(this.channelService.findByCid(tradeOrderReq.getCid()));
		}
		return investorTradeOrderService.saveEntity(orderEntity);
	}
	
	/**
	 * 正常投资
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createNormalInvestTradeOrderRequireNew(TradeOrderReq tradeOrderReq) {
		tradeOrderReq.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_invest);
		InvestorTradeOrderEntity orderEntity = createInvestTradeOrder(tradeOrderReq);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_investor);
		orderEntity.setChannel(this.channelService.findByCid(tradeOrderReq.getCid()));
		return investorTradeOrderService.saveEntity(orderEntity);
	}


	/**
	 *插入快定宝关系记录表
	 *
	 * @author yujianlong
	 * @date 2018/4/2 10:21
	 * @param [tradeOrderReq]
	 * @return com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorOpenCycleRelationEntity createInvestorOpenCycleRelation(InvestorTradeOrderEntity investorTradeOrderEntity) {
		InvestorOpenCycleRelationEntity investorOpenCycleRelationEntity = new InvestorOpenCycleRelationEntity();
		investorOpenCycleRelationEntity.setSourceOrderCode(investorTradeOrderEntity.getOrderCode());
		investorOpenCycleRelationEntity.setPhone(investorTradeOrderEntity.getInvestorBaseAccount().getPhoneNum());
		investorOpenCycleRelationEntity.setOrderType(InvestorOpenCycleRelationEntity.ORDERTYPE_BOOKING);
		investorOpenCycleRelationEntity.setInvestAmount(BigDecimal.ZERO);
		investorOpenCycleRelationEntity.setInvestOrderCode(null);
		investorOpenCycleRelationEntity.setContinueStatus(InvestorOpenCycleRelationEntity.CONTINUESTATUSTYPE_YES);
		investorOpenCycleRelationEntity.setAssignment(InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_DEFAULT);
//		investorOpenCycleRelationEntity.setInvestProductName(investorTradeOrderEntity.getProduct().getName());
		investorOpenCycleRelationEntity.setInvestorOid(investorTradeOrderEntity.getInvestorBaseAccount().getUserOid());
		if (!Objects.equals(investorTradeOrderEntity.getOrderType(),InvestorTradeOrderEntity.TRADEORDER_orderType_invest)){
			investorOpenCycleRelationEntity.setPayType(InvestorOpenCycleRelationEntity.PAYTYPE_T0);
		}else{
			investorOpenCycleRelationEntity.setPayType(InvestorOpenCycleRelationEntity.PAYTYPE_BANK);
		}
		investorOpenCycleRelationEntity.setRedeemAmount(BigDecimal.ZERO);
		investorOpenCycleRelationEntity.setCycleConfirmDate(null);
		//先留空 ORDERSTATUS_APPLYING 已提交等状态
		investorOpenCycleRelationEntity.setOrderStatus(null);
		investorOpenCycleRelationEntity.setSourceOrderAmount(investorTradeOrderEntity.getOrderAmount());
		investorOpenCycleRelationEntity.setRedeemOrderCode(null);
		investorOpenCycleRelationEntity.setCreateTime(investorTradeOrderEntity.getOrderTime());
		return investorOpenCycleService.saveAndFlush(investorOpenCycleRelationEntity);
	}


	
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createNoPayInvestTradeOrderRequireNew(RedeemInvestTradeOrderReq tradeOrderReq, String userOid) {
		return createNoPayInvestTradeOrder(tradeOrderReq, userOid);
	}
	
	/**
	 * 创建赎回投资单 
	 */
	public InvestorTradeOrderEntity createNoPayInvestTradeOrder(RedeemInvestTradeOrderReq tradeOrderReq, String userOid) {
		Product product = this.productService.findByOid(tradeOrderReq.getInvestProductOid());
		/**
		 * 查询赎回的产品
		 * **/
		Product Redeemproduct = this.productService.findByOid(tradeOrderReq.getRedeemProductOid());
		InvestorTradeOrderEntity orderEntity = new InvestorTradeOrderEntity();
		orderEntity.setPublisherBaseAccount(product.getPublisherBaseAccount());
		orderEntity.setInvestorBaseAccount(this.investorBaseAccountService.findByUid(userOid));
		orderEntity.setProduct(product);
		orderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_invest));
		orderEntity.setOrderAmount(tradeOrderReq.getOrderAmount());
		orderEntity.setOrderVolume(tradeOrderReq.getOrderAmount().divide(product.getNetUnitShare()));
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_submitted);
		orderEntity.setContractStatus(InvestorTradeOrderEntity.TRADEORDER_contractStatus_toHtml);
		orderEntity.setCheckStatus(InvestorTradeOrderEntity.TRADEORDER_checkStatus_no);
		orderEntity.setPayChannel(Redeemproduct.getName());
		orderEntity.setOrderTime(new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis()+1000)); // 订单时间
		orderEntity.setCreateTime(new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis()+1000)); // 取应用服务器时间+1s为了比cash订单时间大
		if(!"".equals(tradeOrderReq.getCid()) && tradeOrderReq.getCid() != null){
			orderEntity.setChannel(this.channelService.findByCid(tradeOrderReq.getCid()));
		}
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest);
		tradeOrderReq.setOrderType(orderEntity.getOrderType());
		String productType= Optional.ofNullable(product).map(Product::getType).map(Dict::getOid).orElse("");
		if (Objects.equals(Product.TYPE_Producttype_04,productType)){
			throw new GHException("产品信息有误,请选择其他产品购买");
		}
		//这里注意03产品需要设置PayAmount 为orderAmount
		if (!Objects.equals(Product.TYPE_Producttype_03,productType)){
			setCouponProperties(orderEntity, tradeOrderReq.getCouponId(), tradeOrderReq.getCouponType(), tradeOrderReq.getPayAmouont(), tradeOrderReq.getCouponDeductibleAmount(), tradeOrderReq.getCardId());
			/** 校验卡券信息 */
			tulipService.validateCouponForRmInvest(tradeOrderReq);

		}else{
			orderEntity.setPayAmount(
					orderEntity.getOrderAmount() == null ? BigDecimal.ZERO : orderEntity.getOrderAmount());// 实际支付金额
		}
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_investor);
		return this.investorTradeOrderService.saveEntity(orderEntity);
	}
	
	/**
	 * 创建冲销单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createWriteOffTradeOrder(TradeOrderReq tradeOrderReq) {
		tradeOrderReq.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff);
		InvestorTradeOrderEntity orderEntity = createInvestTradeOrder(tradeOrderReq);
		orderEntity.setRelateOid(tradeOrderReq.getOrderOid());
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform); // 订单创建人
		return investorTradeOrderService.saveEntity(orderEntity);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: createInvestProfitTradeOrder
	 * @Description: 创建二级邀请奖励收益投资订单
	 * @param tradeOrderReq
	 * @return InvestorTradeOrderEntity
	 * @date 2017年6月14日 下午5:12:14
	 * @since  1.0.0
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createInvestProfitTradeOrder(TradeOrderReq tradeOrderReq) {
		tradeOrderReq.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_profitInvest);
		InvestorTradeOrderEntity orderEntity = createInvestTradeOrder(tradeOrderReq);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_investor);
		return investorTradeOrderService.saveEntity(orderEntity);
	}
	
	/**
	 * 设置卡券相关属性
	 */
	private void setCouponProperties(InvestorTradeOrderEntity orderEntity, String couponId, String couponType, BigDecimal payAmount, BigDecimal couponDeductibleAmount, Integer cardId) {
		
		// 没有使用卡券时,实付金额等于订单金额
		if (this.tulipSDKService.isSdkEnable() && this.tulipSDKService.isUseCoupon(couponId)) {
			if (!InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())) {
				if (VersionUtils.checkVersionV160()) {
					logger.info("setCouponProperties-old");
					// //新手标、竞猜宝、活期不能使用卡券
					if ((!InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldInvest.equals(orderEntity.getOrderType())
							&& Product.TYPE_Producttype_02.equals(orderEntity.getProduct().getType().getOid()))
							|| labelService.isProductLabelHasAppointLabel(orderEntity.getProduct().getProductLabel(),
									LabelEnum.newbie.toString())
							|| orderEntity.getProduct().getGuess() != null) {
						// 活期产品、竞猜活动产品、新手标产品不允许使用红包
						throw new AMPException(18004);
					}
				} else {
					logger.info("setCouponProperties-new");
					// 校验产品、红包关联
					if (InvestorCouponOrderEntity.TYPE_couponType_redPackets.equals(couponType)) {
						if (ProductCouponEnum.notUseRedCoupon.getCode()
								.equals(orderEntity.getProduct().getUseRedPackages())) {
							throw new AMPException("该产品不能使用红包");
						}
						if (ProductCouponEnum.useRedCoupon.getCode().equals(orderEntity.getProduct().getUseRedPackages())
								&& !productCouponService.canUserCardByProductOid(orderEntity.getProduct().getOid(), cardId,
										ProductCouponEnum.redCoupon.getCode())) {
							throw new AMPException("该产品不能使用此红包");
						}
					} else if (InvestorCouponOrderEntity.TYPE_couponType_rateCoupon.equals(couponType)) {
						if (ProductCouponEnum.notUseRaiseRateCoupon.getCode()
								.equals(orderEntity.getProduct().getUseraiseRateCoupons())) {
							throw new AMPException("该产品不能使用加息券");
						}
						if (ProductCouponEnum.useRaiseRateCoupon.getCode()
								.equals(orderEntity.getProduct().getUseraiseRateCoupons())
								&& !productCouponService.canUserCardByProductOid(orderEntity.getProduct().getOid(), cardId,
										ProductCouponEnum.raiseRateCoupon.getCode())) {
							throw new AMPException("该产品不能使用此加息券");
						}
					}
				}
			}
			orderEntity.setCoupons(couponId);// 卡券编号
			orderEntity.setCouponType(couponType);// 卡券类型
			orderEntity.setCouponAmount(couponDeductibleAmount == null ? BigDecimal.ZERO : couponDeductibleAmount);// 卡券实际抵扣金额
			orderEntity.setPayAmount(payAmount == null ? BigDecimal.ZERO : payAmount);// 实际支付金额
		} else {
			orderEntity.setPayAmount(
					orderEntity.getOrderAmount() == null ? BigDecimal.ZERO : orderEntity.getOrderAmount());// 实际支付金额
		}
	}

	/**
	 * 创建投资者投资订单
	 */
	private InvestorTradeOrderEntity createInvestTradeOrder(TradeOrderReq tradeOrderReq) {

		Product product = this.productService.findByOid(tradeOrderReq.getProductOid());

		InvestorTradeOrderEntity orderEntity = new InvestorTradeOrderEntity();
		orderEntity.setPublisherBaseAccount(product.getPublisherBaseAccount());
		orderEntity.setInvestorBaseAccount(this.investorBaseAccountService.findByUid(tradeOrderReq.getUid()));
		orderEntity.setProduct(product);
		orderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_invest));
		orderEntity.setOrderAmount(tradeOrderReq.getMoneyVolume());
		orderEntity.setOrderVolume(tradeOrderReq.getMoneyVolume().divide(product.getNetUnitShare()));
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_submitted);
		orderEntity.setContractStatus(InvestorTradeOrderEntity.TRADEORDER_contractStatus_toHtml);
		orderEntity.setCheckStatus(InvestorTradeOrderEntity.TRADEORDER_checkStatus_no);
		orderEntity.setOrderTime(DateUtil.getSqlCurrentDate()); // 订单时间
		orderEntity.setOrderType(tradeOrderReq.getOrderType());
		tradeOrderReq.setDurationPeriodDays(orderEntity.getProduct().getDurationPeriodDays());
		String productType= Optional.ofNullable(product).map(Product::getType).map(Dict::getOid).orElse("");
		if (Objects.equals(Product.TYPE_Producttype_04,productType)){
			throw new GHException("产品信息有误,请选择其他产品购买");
		}
		if (!Objects.equals(Product.TYPE_Producttype_03,productType)){
			setCouponProperties(orderEntity, tradeOrderReq.getCouponId(), tradeOrderReq.getCouponType(), tradeOrderReq.getPayAmouont(), tradeOrderReq.getCouponDeductibleAmount(), tradeOrderReq.getCardId());
			/** 校验卡券信息 */
			tulipService.validateCouponForInvest(tradeOrderReq);
		}
		return orderEntity;
	}
	/**
	 * 创建自动申购单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createAutoTradeOrder(TradeOrderReq tradeOrderReq) {
		tradeOrderReq.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_invest);
		InvestorTradeOrderEntity orderEntity = createInvestTradeOrder(tradeOrderReq);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_investor);
//		orderEntity.setChannel(this.channelService.findByCid(tradeOrderReq.getCid()));
		orderEntity.setIsAuto(FamilyEnum.isAuto0.getCode());//自动扣款订单
		orderEntity.setProtocalOid(tradeOrderReq.getProtocalOid());
		return investorTradeOrderService.saveEntity(orderEntity);
	}
}