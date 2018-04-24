package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.util.Date;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.guohuai.component.exception.AMPException;
import com.guohuai.mmp.platform.publisher.offset.VolumeConfirmRep;
import com.guohuai.mmp.publisher.hold.PublisherHoldDao;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotService;
import com.guohuai.mmp.publisher.investor.holdapartincome.PartIncomeEntity;
import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.ams.channel.ChannelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.cache.service.CacheChannelService;
import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.abandonlog.AbandonLogService;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.cashflow.InvestorCashFlowService;
import com.guohuai.mmp.investor.orderlog.OrderLogEntity;
import com.guohuai.mmp.investor.orderlog.OrderLogService;
import com.guohuai.mmp.platform.accment.AccParam;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.accment.TradeRequest;
import com.guohuai.mmp.platform.finance.modifyorder.ModifyOrderNewService;
import com.guohuai.mmp.platform.finance.result.PlatformFinanceCompareDataResultNewService;
import com.guohuai.mmp.platform.payment.OrderNotifyReq;
import com.guohuai.mmp.platform.payment.PayParam;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;
import com.guohuai.mmp.platform.payment.RedeemPayRequest;
import com.guohuai.mmp.platform.publisher.offset.OffsetService;
import com.guohuai.mmp.platform.tulip.TulipService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.serialtask.RedeemNotifyParams;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskService;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class InvestorRedeemTradeOrderService {
	Logger logger = LoggerFactory.getLogger(InvestorRedeemTradeOrderService.class);
	
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private ChannelService channelService; 
	@Autowired
	private ProductService productService;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private PlatformFinanceCompareDataResultNewService platformFinanceCompareDataResultNewService;
	@Autowired
	private ModifyOrderNewService modifyOrderNewService;
	@Autowired
	private AbandonLogService abandonLogService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private InvestorCashFlowService investorCashFlowService;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	@Autowired
	private OrderLogService orderLogService;
	@Autowired
	private OffsetService offsetService;
	@Autowired
	private TulipService tulipService;
	@Autowired
	private AccmentService accmentService;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private CacheHoldService cacheHoldService;
	@Autowired
	private CacheChannelService cacheChannelService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private InvestorSpecialRedeemService investorSpecialRedeemService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private MessageSendUtil messageSendUtil;
	@Autowired
	private OrderDateService orderDateService;
	@Autowired
	private PublisherHoldDao publisherHoldDao;
	@Autowired
	private InvestorBfPlusRedeemService investorBfPlusRedeemService;
	@Autowired
	private TradeCalendarService tradeCalendarService;
    @Autowired
    private InvestorOpenCycleService investorOpenCycleService;
    @Autowired
    private SnapshotService snapshotService;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public TradeOrderRep redeemRequiresNew(String orderCode) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		return this.redeem(orderEntity);
	}

	public TradeOrderRep redeem(InvestorTradeOrderEntity orderEntity) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();

		verification(orderEntity);

		serial(orderEntity);

		tradeOrderRep.setOrderStatus(orderEntity.getOrderStatus());
		tradeOrderRep.setTradeOrderOid(orderEntity.getOid());
		return tradeOrderRep;
	}

//	public void specialRedeem(InvestorTradeOrderEntity orderEntity){
//		TradeOrderRep tradeOrderRep = new TradeOrderRep();
//		try {
//			tradeOrderRep = investorRedeemTradeOrderService.redeemDoRequireNew(orderEntity.getOrderCode());
//			logger.info("=====订单赎回日志记录====tradeOrderRep:::" + JSON.toJSONString(tradeOrderRep));
//		} catch (Exception e) {
//			e.printStackTrace();
//			tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused);
//			tradeOrderRep.setErrorCode(BaseRep.ERROR_CODE);
//			tradeOrderRep.setErrorMessage(AMPException.getStacktrace(e));
//		}
//		// 赎回日志记录
//		investorRedeemTradeOrderService.redeemThen(tradeOrderRep, orderEntity.getOrderCode());
//	}
	
	
//	@Transactional(value = TxType.REQUIRES_NEW)
//	public TradeOrderRep redeemRequiresNew(String orderCode) {
//		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
//		return this.redeem(orderEntity);
//	}
	
//	public TradeOrderRep redeem(InvestorTradeOrderEntity orderEntity) {
//		TradeOrderRep tradeOrderRep = new TradeOrderRep();
//		
//		verification(orderEntity);
//		
//		serial(orderEntity);
//		
//		tradeOrderRep.setOrderStatus(orderEntity.getOrderStatus());
//		tradeOrderRep.setTradeOrderOid(orderEntity.getOid());
//		return tradeOrderRep;
//	}
	
	private void serial(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			RedeemNotifyParams params = new RedeemNotifyParams();
			params.setOrderCode(orderEntity.getOrderCode());
			SerialTaskReq<RedeemNotifyParams> sreq = new SerialTaskReq<RedeemNotifyParams>();
			sreq.setTaskParams(params);
			sreq.setTaskCode(SerialTaskEntity.TASK_taskCode_redeem);
			serialTaskService.createSerialTask(sreq);
		}else if (InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem.equals(orderEntity.getOrderType())) {
			RedeemNotifyParams params = new RedeemNotifyParams();
			params.setOrderCode(orderEntity.getOrderCode());
			SerialTaskReq<RedeemNotifyParams> sreq = new SerialTaskReq<RedeemNotifyParams>();
			sreq.setTaskParams(params);
			sreq.setTaskCode(SerialTaskEntity.TASK_taskCode_specialRedeem);
			serialTaskService.createSerialTask(sreq);
		}
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public TradeOrderRep redeemDoRequireNew(String orderCode) {
		return this.redeemDo(orderCode);
	}

	
	public TradeOrderRep redeemDo(String orderCode) {
		
		TradeOrderRep tradeOrderRep = new TradeOrderRep();

		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		if(InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem.equals(orderEntity.getOrderType())){
		    return this.plusRedeemDo(orderEntity);
        }
		
		/** 活转定赎回订单不校验产品单日赎回上限、单人单日赎回上限、单人单日赎回次数 **/
		if(!InvestorTradeOrderEntity.TRADEORDER_orderType_noPayRedeem.equals(orderEntity.getOrderType())){
			/** 产品单日净赎回上限 **/
			this.productService.update4Redeem(orderEntity.getProduct(), orderEntity.getOrderVolume());
			/** 单人单日赎回上限、单人单日赎回次数 **/
			this.publisherHoldService.redeemDayRules(orderEntity);
		}

		tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_accepted);
		
		/** 份额确认 */
		this.confirm(orderEntity);
				
		tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed);
		
		redeemPay(orderEntity);

		selfNotify(orderEntity);
		
		//不是拒绝状态的订单发送消息
		if(InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
				||InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())
				||InvestorTradeOrderEntity.TRADEORDER_orderType_writeOff.equals(orderEntity.getOrderType())){
			if(!InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused.equals(tradeOrderRep.getOrderStatus())){
				sendMessage(orderEntity, DealMessageEnum.REDEEM_APPLY.name());
			}
		}

		return tradeOrderRep;

	}

	public TradeOrderRep plusRedeemDo(InvestorTradeOrderEntity redeemOrderEntity) {
		TradeOrderRep tradeOrderRep = new TradeOrderRep();
		tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_accepted);

		/** 份额确认 */
		this.plusConfirm(redeemOrderEntity);

		tradeOrderRep.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed);

        plusSelfNotify(redeemOrderEntity);

		//不是拒绝状态的订单发送消息
        if(!InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused.equals(tradeOrderRep.getOrderStatus())){
            if(!InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem.equals(redeemOrderEntity.getOrderType())){
                sendMessage(redeemOrderEntity, DealMessageEnum.REDEEM_APPLY.name());
            }
        }

		return tradeOrderRep;
	}

	// 将赎回订单表的份额扣减并判断是否冲销完成，冲销完成则将用户状态置为正常
	public void updateSpecialRedeemAmount(String userId,BigDecimal orderAmount){
		this.investorSpecialRedeemService.updateLeftSpecialRedeemAmount(userId, orderAmount);
	}
	
	/**
	 * 赎回确认
	 */
	public void confirm(InvestorTradeOrderEntity orderEntity) {
		offsetService.processItem(orderEntity);
	}
	public void plusConfirm(InvestorTradeOrderEntity orderEntity) {
		offsetService.plusProcessItem(orderEntity, new VolumeConfirmRep());
	}

    public void plusVerification(PlusRedeemAfterReq plusRedeemAfterReq) {
        InvestorTradeOrderEntity orderEntity = plusRedeemAfterReq.getRedeemOrderEntity();
        /** 产品相关交易约束 **/
        this.cacheProductService.checkProduct4PlusRedeem(orderEntity);
        // 快定宝持仓修改
        int i = this.publisherHoldDao.bfPlusRedeem(plusRedeemAfterReq.getBaseAmount(),
                orderEntity.getProduct().getNetUnitShare(), orderEntity.getInvestorBaseAccount().getOid(),
                orderEntity.getProduct().getOid(), plusRedeemAfterReq.getExpectIncome(), plusRedeemAfterReq.getIncome());
        if (i < 1) {
            // error.define[20005]=赎回份额异常(CODE:20005)
            throw AMPException.getException(20005);
        }
    }

	public void verification(InvestorTradeOrderEntity orderEntity) {
		/** 产品相关交易约束 **/
		this.cacheProductService.checkProduct4Redeem(orderEntity);
		/**
		 * 提现下单时先扣减数据库用户持有份额
		 * 解决问题：
		 * 1. 扣减异常回滚问题；
		 * 2. 用户赎回后10s刷缓存，缓存份额刷回提现前，加上在途份额导致赎回份额显示增加问题
		 */
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType()) || InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			String holdStatus = this.publisherHoldService.getFlatWareHoldStatus(orderEntity);
			int i = this.publisherHoldDao.normalRedeem(orderEntity.getOrderVolume(),
					orderEntity.getProduct().getNetUnitShare(), orderEntity.getInvestorBaseAccount().getOid(),
					orderEntity.getProduct().getOid(), holdStatus);
			if (i < 1) {
				// error.define[20005]=赎回份额异常(CODE:20005)
				throw AMPException.getException(20005);
			}

		}
		if(!(InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem.equals(orderEntity.getOrderType()) || InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderEntity.getOrderType()))){
			/** 活转定订单不校验产品单日赎回上限、单人单日赎回上限、单人单日赎回次数 **/
			if(!InvestorTradeOrderEntity.TRADEORDER_orderType_noPayRedeem.equals(orderEntity.getOrderType())){
				/** 产品单日净赎回上限 **/
				this.cacheProductService.update4Redeem(orderEntity);
				/** 单人单日赎回上限、单人单日赎回次数 **/
				this.cacheHoldService.redeemDayRules(orderEntity);
				/** 单人单日在平台赎回次数、限额 **/
				this.cacheHoldService.validDailyInvestorRedeemLimit(orderEntity);
			}
			/** 个人赎回份额张约束 */
			this.cacheHoldService.update4MinRedeem(orderEntity);
		}
		/** 校验渠道 */
		if (InvestorTradeOrderEntity.TRADEORDER_createMan_investor.equals(orderEntity.getCreateMan())) {
			cacheChannelService.checkChannel(orderEntity.getChannel().getCid(), orderEntity.getChannel().getCkey(),
					orderEntity.getProduct().getOid());
		}
		
		/** 锁仓,判断可赎回份额是否足够 */
		this.cacheHoldService.redeemLock(orderEntity);
	}
	
	/**
	 * 订单赎回日志记录
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void redeemThen(TradeOrderRep rep, String orderCode) {
		
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		logger.info("=====订单赎回日志记录====rep:::" + JSON.toJSONString(rep));
		logger.info("=====订单赎回日志记录====orderEntity:::" + JSON.toJSONString(orderEntity));
		
//		orderEntity.setOrderStatus(rep.getOrderStatus());
//		investorTradeOrderService.saveEntity(orderEntity);

		OrderLogEntity orderLog = new OrderLogEntity();
		orderLog.setErrorCode(rep.getErrorCode());
		orderLog.setErrorMessage(rep.getErrorMessage());
		orderLog.setOrderType(orderEntity.getOrderType());
		orderLog.setTradeOrderOid(orderEntity.getOrderCode());
		orderLog.setOrderStatus(orderEntity.getOrderStatus());
		OrderLogEntity redeemReqOrderlog = this.orderLogService.create(orderLog);
		logger.info("======赎回订单日志======OrderLogEntity:" + JSON.toJSONString(redeemReqOrderlog));
	}
	
	/**
	 * 正常赎回单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createNormalRedeemTradeOrder(RedeemTradeOrderReq req) {
		logger.info("=======创建赎回订单开始, 参数:" + JSON.toJSONString(req));
		InvestorTradeOrderEntity orderEntity = this.createRedeemTradeOrder(req);
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_investor);
		orderEntity.setChannel(this.channelService.findByCid(req.getCid())); //所属渠道
		orderEntity.setProvince(req.getProvince());
		orderEntity.setCity(req.getCity());
		logger.info("=======创建赎回订单Save后返回对象, orderEntity:" + JSON.toJSONString(orderEntity));
		return orderEntity;
	}

	/**
	 * 快定宝赎回单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public PlusRedeemAfterReq createBfPlusRedeemTradeOrder(PlusRedeemTradeOrderReq req) {
		InvestorTradeOrderEntity investOrderEntity = investorTradeOrderService.findByOrderCode(req.getOrderCode());
		logger.info("=======创建赎回订单开始, 参数:" + JSON.toJSONString(req));
		Product product = this.productService.findByOid(req.getProductOid());
		if(DateUtil.daysBetween(product.getDurationPeriodEndDate()) > 0){
			throw new AMPException(4, "存续期已过，无法赎回");
		}
        // 如果部分赎回：根据原订单生成新的投资订单，状态保持原订单状态。关联原订单。
        BigDecimal difference = investOrderEntity.getOrderAmount().subtract(req.getBaseAmount());
        if(difference.compareTo(product.getInvestMin()) < 0 && difference.compareTo(BigDecimal.ZERO) != 0){
            throw new AMPException(3, "好尴尬呀！转出后剩余本金不能小于最低起投额，您可以调整转出本金或全部赎回。");
        }
        if(InvestorTradeOrderEntity.TRADEORDER_orderStatus_invalidate.equals(investOrderEntity.getOrderStatus())){
            throw new AMPException(-1, "该订单已作废，不可赎回！");
        }

		InvestorTradeOrderEntity redeemOrderEntity = new InvestorTradeOrderEntity();
		InvestorBaseAccountEntity baseAccount = this.investorBaseAccountService.findByUid(req.getUid());
		redeemOrderEntity.setInvestorBaseAccount(baseAccount); //所属投资人
		redeemOrderEntity.setPublisherBaseAccount(product.getPublisherBaseAccount()); //所属发行人
		redeemOrderEntity.setProduct(product); //所属产品
		redeemOrderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_redeem));
		//orderAmount=本金+利息-手续费
		// 利息（收益）
		BigDecimal income = req.getBaseAmount().multiply(product.getBasicRatio()).divide(new BigDecimal(product.getIncomeCalcBasis()), 8, BigDecimal.ROUND_DOWN)
				.multiply(BigDecimal.valueOf(DateUtil.daysBetween(investOrderEntity.getConfirmDate()))).setScale(2, BigDecimal.ROUND_DOWN);
		// 手续费
		BigDecimal fee = BigDecimal.ZERO;
		// 锁定期内有手续费
		if(InvestorTradeOrderEntity.TRADEORDER_orderType_changeInvest.equals(investOrderEntity.getOrderType())
				&& (DateUtil.daysBetween(investOrderEntity.getConfirmDate()) + 1) <= investOrderEntity.getProduct().getMovedupRedeemLockDays()){
			if(req.getBaseAmount().compareTo(BigDecimal.valueOf(2000)) < 0){
				fee = product.getMovedupRedeemMinPay();
			}else {
				fee = req.getBaseAmount().multiply(BigDecimal.valueOf(0.001));
			}
		}
		// 校验最低赎回2元
		if ((fee.compareTo(BigDecimal.ZERO) > 0) && (req.getBaseAmount().compareTo(product.getMovedupRedeemMinPay()) < 0)){
			throw new AMPException(5, "转出本金需大于2元");
		}
		BigDecimal orderAmount = req.getBaseAmount().add(income).subtract(fee);
		redeemOrderEntity.setOrderAmount(orderAmount);
		redeemOrderEntity.setPayAmount(redeemOrderEntity.getOrderAmount());
		redeemOrderEntity.setOrderVolume(orderAmount.divide(product.getNetUnitShare()));
		redeemOrderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed);
		redeemOrderEntity.setCheckStatus(InvestorTradeOrderEntity.TRADEORDER_checkStatus_no);
		redeemOrderEntity.setOrderTime(DateUtil.getSqlCurrentDate());
		redeemOrderEntity.setCreateTime(DateUtil.getSqlCurrentDate());//取应用服务器时间，为了和noPayInvest订单的createTime相差1s
		redeemOrderEntity.setTotalBaseIncome(income);
		redeemOrderEntity.setTotalIncome(income);
        redeemOrderEntity.setConfirmDate(DateUtil.getSqlDate());
		InvestorTradeOrderEntity newRedeemOrderEntity = investorTradeOrderService.saveEntity(redeemOrderEntity);

		newRedeemOrderEntity.setOrderType(req.getOrderType());
		newRedeemOrderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_investor);
		newRedeemOrderEntity.setChannel(this.channelService.findByCid(req.getCid())); //所属渠道
		logger.info("=======创建赎回订单Save后返回对象, orderEntity:" + JSON.toJSONString(redeemOrderEntity));

        PlusRedeemAfterReq plusRedeemAfterReq = new PlusRedeemAfterReq();
        plusRedeemAfterReq.setBaseAmount(req.getBaseAmount());
        plusRedeemAfterReq.setDifference(difference);
        plusRedeemAfterReq.setFee(fee);
        plusRedeemAfterReq.setInvestOrderCode(req.getOrderCode());
        plusRedeemAfterReq.setProduct(product);
        plusRedeemAfterReq.setRedeemOrderEntity(newRedeemOrderEntity);
        plusRedeemAfterReq.setIncome(income);
        plusRedeemAfterReq.setInvestorOid(req.getUid());
        if(difference.compareTo(plusRedeemAfterReq.getProduct().getInvestMin()) > 0){
            BigDecimal expectIncome = difference.multiply(product.getBasicRatio()).divide(new BigDecimal(product.getIncomeCalcBasis()), 8, BigDecimal.ROUND_DOWN)
                    .multiply(BigDecimal.valueOf(product.getDurationPeriodDays())).setScale(2, BigDecimal.ROUND_DOWN);
            plusRedeemAfterReq.setExpectIncome(expectIncome);
        }
		return plusRedeemAfterReq;
	}

    @Transactional(value = TxType.REQUIRES_NEW)
    public TradeOrderRep plusRedeemRequiresNew(PlusRedeemAfterReq plusRedeemAfterReq) {
        InvestorTradeOrderEntity orderEntity = plusRedeemAfterReq.getRedeemOrderEntity();
        TradeOrderRep tradeOrderRep = new TradeOrderRep();
        plusVerification(plusRedeemAfterReq);
        tradeOrderRep.setOrderStatus(orderEntity.getOrderStatus());
        tradeOrderRep.setTradeOrderOid(orderEntity.getOid());
        plusRedeemAfter(plusRedeemAfterReq);
        return tradeOrderRep;
    }

    /**
     * 赎回快定宝动账成功后需要的功能
     * @param plusRedeemAfterReq
     */
    private void plusRedeemAfter(PlusRedeemAfterReq plusRedeemAfterReq) {
        InvestorTradeOrderEntity savedNewInvestOrderEntity = null;
        InvestorTradeOrderEntity investOrderEntity = investorTradeOrderService.findByOrderCode(plusRedeemAfterReq.getInvestOrderCode());

        BigDecimal difference = plusRedeemAfterReq.getDifference();
        if(difference.compareTo(plusRedeemAfterReq.getProduct().getInvestMin()) < 0 && difference.compareTo(BigDecimal.ZERO) != 0){
            throw new AMPException(3, "好尴尬呀！转出后剩余本金不能小于最低起投额，您可以调整转出本金或全部赎回。");
        }
        if(difference.compareTo(plusRedeemAfterReq.getProduct().getInvestMin()) > 0){
            InvestorTradeOrderEntity newInvestOrderEntity = new InvestorTradeOrderEntity();
            BeanUtils.copyProperties(investOrderEntity, newInvestOrderEntity);
            newInvestOrderEntity.setOid(null);
            newInvestOrderEntity.setOrderAmount(plusRedeemAfterReq.getDifference());
            newInvestOrderEntity.setPayAmount(plusRedeemAfterReq.getDifference());
            newInvestOrderEntity.setOrderVolume(plusRedeemAfterReq.getDifference().divide(plusRedeemAfterReq.getProduct().getNetUnitShare()));
            newInvestOrderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_invest));
            newInvestOrderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed);
            newInvestOrderEntity.setExpectIncome(plusRedeemAfterReq.getExpectIncome());
            newInvestOrderEntity.setExpectIncomeExt(plusRedeemAfterReq.getExpectIncome());
            newInvestOrderEntity.setHoldVolume(plusRedeemAfterReq.getDifference());
            newInvestOrderEntity.setValue(plusRedeemAfterReq.getDifference());
            savedNewInvestOrderEntity = investorTradeOrderService.saveEntity(newInvestOrderEntity);
        }

        InvestorOpenCycleRelationEntity sourceRelationEntity = investorOpenCycleService.findBySourceOrderCode(plusRedeemAfterReq.getInvestOrderCode());
        sourceRelationEntity.setRedeemOrderCode(plusRedeemAfterReq.getRedeemOrderEntity().getOrderCode());
        sourceRelationEntity.setRedeemAmount(plusRedeemAfterReq.getRedeemOrderEntity().getOrderAmount());
        if(savedNewInvestOrderEntity == null){
            sourceRelationEntity.setAssignment(InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_ALLREDEEM);
        }else {
            InvestorOpenCycleRelationEntity newRelationEntity = new InvestorOpenCycleRelationEntity();
            newRelationEntity.setSourceOrderCode(savedNewInvestOrderEntity.getOrderCode());
            newRelationEntity.setSourceOrderAmount(savedNewInvestOrderEntity.getOrderAmount());
            newRelationEntity.setAssignment(InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_DEFAULT);
            newRelationEntity.setCreateTime(savedNewInvestOrderEntity.getOrderTime());

            newRelationEntity.setInvestorOid(sourceRelationEntity.getInvestorOid());
            newRelationEntity.setPhone(sourceRelationEntity.getPhone());
            newRelationEntity.setOrderType(sourceRelationEntity.getOrderType());
            newRelationEntity.setPayType(sourceRelationEntity.getPayType());
            newRelationEntity.setContinueStatus(sourceRelationEntity.getContinueStatus());
            newRelationEntity.setCycleConfirmDate(sourceRelationEntity.getCycleConfirmDate());
            newRelationEntity.setInvestProductName(sourceRelationEntity.getInvestProductName());

            investorOpenCycleService.saveAndFlush(newRelationEntity);

            sourceRelationEntity.setAssignment(InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_PARTREDEEM);
            sourceRelationEntity.setInvestOrderCode(savedNewInvestOrderEntity.getOrderCode());
            sourceRelationEntity.setInvestAmount(savedNewInvestOrderEntity.getOrderAmount());

        }
        investorOpenCycleService.saveAndFlush(sourceRelationEntity);

        // 修改原订单为已失效
        investorTradeOrderService.invalidateTradeOrder(investOrderEntity.getOid());

        // 将赎回单增加到15天定期赎回列表，根据T⽇⽣成payDate
        Date payDate = tradeCalendarService.nextTrade(new java.sql.Date(System.currentTimeMillis()), 1);
        InvestorBfPlusRedeemEntity entity = new InvestorBfPlusRedeemEntity();
        entity.setOid(plusRedeemAfterReq.getRedeemOrderEntity().getOid());
        entity.setPayDate(payDate);
        entity.setFee(plusRedeemAfterReq.getFee());
        entity.setBaseAmount(plusRedeemAfterReq.getBaseAmount());
        entity.setCreateDate(plusRedeemAfterReq.getRedeemOrderEntity().getCreateTime());
        entity.setStatus(0);
        investorBfPlusRedeemService.save(entity);
        distributeCycleProductInterest(plusRedeemAfterReq);
        this.plusSendMessage(plusRedeemAfterReq.getRedeemOrderEntity(), DealMessageEnum.PLUS_REDEEM_APPLY.name());
    }

    private void plusSendMessage(InvestorTradeOrderEntity orderEntity, String tag) {
        DealMessageEntity messageEntity = new DealMessageEntity();
        messageEntity.setPhone(orderEntity.getInvestorBaseAccount().getPhoneNum());
        messageEntity.setUserName(orderEntity.getInvestorBaseAccount().getRealName());
        messageEntity.setOrderAmount(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
        messageEntity.setOrderType(orderEntity.getOrderType());
        messageEntity.setOrderTime(orderEntity.getOrderTime());
        messageEntity.setProductName(orderEntity.getProduct().getName());
        messageEntity.setUserOid(orderEntity.getInvestorBaseAccount().getOid());
        messageEntity.setOrderCode(orderEntity.getOrderCode());
        messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
    }

    /**
     * 收益明细
     */
    public void distributeCycleProductInterest(PlusRedeemAfterReq plusRedeemAfterReq){
        //String holdOid, String productOid, String investorOid, BigDecimal totalIncome, BigDecimal orderAmount
        PublisherHoldEntity holdEntity = publisherHoldDao.findByInvestorAndProduct(plusRedeemAfterReq.getInvestorOid(), plusRedeemAfterReq.getProduct().getOid());
        InvestorIncomeEntity investorIncomeEntity =
                snapshotService.getCycleProductInterest(plusRedeemAfterReq.getProduct().getOid(), plusRedeemAfterReq.getInvestorOid(), DateUtil.getSqlDate());
        if(investorIncomeEntity == null){
            snapshotService.distributeCycleProductInterest(holdEntity.getOid(), plusRedeemAfterReq.getProduct().getOid(), plusRedeemAfterReq.getInvestorOid()
                    ,plusRedeemAfterReq.getIncome(), plusRedeemAfterReq.getBaseAmount());
        }else {
            snapshotService.updateCycleProductInterest(investorIncomeEntity.getOid(), plusRedeemAfterReq.getIncome(), plusRedeemAfterReq.getBaseAmount());
        }

        snapshotService.distributeCycleProductInterestIncome(plusRedeemAfterReq.getRedeemOrderEntity().getOid(), plusRedeemAfterReq.getBaseAmount());
    }

    /**
	 * 节节高赎回单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createIncrementRedeemTradeOrder(RedeemTradeOrderReq req) {
		InvestorTradeOrderEntity orderEntity = this.createRedeemTradeOrder(req);
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_investor);
		orderEntity.setChannel(this.channelService.findByCid(req.getCid())); //所属渠道
		orderEntity.setProvince(req.getProvince());
		orderEntity.setCity(req.getCity());
		orderEntity.setPayDate(orderDateService.getRedeemDate(orderEntity.getProduct(), orderEntity.getOrderTime()));
		return orderEntity;
	}
	
	/**
	 * 特殊赎回单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createSpecialRedeemTradeOrder(SpecialRedeemOrderReq req) {
		InvestorTradeOrderEntity orderEntity = this.createRedeemTradeOrder(req);
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem);
		orderEntity.setCreateMan(req.getLoginUser());
//		orderEntity.setChannel(this.channelService.findByCid(req.getCid())); //所属渠道
		return orderEntity;
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createNoPayRedeemTradeOrder(RedeemTradeOrderReq req) {
		InvestorTradeOrderEntity orderEntity = this.createRedeemTradeOrder(req);
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_noPayRedeem);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_investor);
		orderEntity.setChannel(this.channelService.findByCid(req.getCid())); //所属渠道
		
		return orderEntity;
	}
	
	/**
	 * 补赎回单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorTradeOrderEntity createReRedeemTradeOrder(RedeemTradeOrderReq req) {
		InvestorTradeOrderEntity orderEntity = this.createRedeemTradeOrder(req);
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_reRedeem);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		orderEntity.setOrderTime(DateUtil.getReInvestOrderTime());
		return orderEntity;
	}
	
	/**
	 * 还本付息单
	 */
	public InvestorTradeOrderEntity createCashTradeOrder(RedeemTradeOrderReq req) {
		InvestorTradeOrderEntity orderEntity = this.createRedeemTradeOrder(req);
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_cash);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		return orderEntity;
	}
	
	/**
	 * 募集失败退款单
	 */
	public InvestorTradeOrderEntity createCashFailTradeOrder(RedeemTradeOrderReq req) {
		InvestorTradeOrderEntity orderEntity = this.createRedeemTradeOrder(req);
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_cashFailed);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		return orderEntity;
	}
	
	/**
	 * 体验金赎回
	 */
	public InvestorTradeOrderEntity createExpRedeemTradeOrder(InvestorTradeOrderEntity orderInEntity) {
		Product product = orderInEntity.getProduct();
		InvestorTradeOrderEntity orderEntity = new InvestorTradeOrderEntity();
		orderEntity.setInvestorBaseAccount(orderInEntity.getInvestorBaseAccount()); //所属投资人
		orderEntity.setPublisherBaseAccount(product.getPublisherBaseAccount()); //所属发行人
		orderEntity.setProduct(product); //所属产品
		orderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_redeem));
		orderEntity.setOrderAmount(orderInEntity.getOrderAmount());
		orderEntity.setOrderVolume(orderInEntity.getOrderVolume());
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_submitted);
		orderEntity.setOrderTime(DateUtil.getSqlCurrentDate());
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_expGoldRedeem);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_done);
		investorTradeOrderService.saveEntity(orderEntity);
		return orderEntity;
	}
	
	/**
	 * 退款单
	 */
	public InvestorTradeOrderEntity createRefundTradeOrder(RedeemTradeOrderReq req) {
		InvestorTradeOrderEntity orderEntity = this.createRedeemTradeOrder(req);
		orderEntity.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_refund);
		orderEntity.setCreateMan(InvestorTradeOrderEntity.TRADEORDER_createMan_platform);
		return orderEntity;
	}
	
	private InvestorTradeOrderEntity createRedeemTradeOrder(RedeemTradeOrderReq req) {
		Product product = this.productService.findByOid(req.getProductOid());
		InvestorTradeOrderEntity orderEntity = new InvestorTradeOrderEntity();
		InvestorBaseAccountEntity baseAccount = this.investorBaseAccountService.findByUid(req.getUid());
		orderEntity.setInvestorBaseAccount(baseAccount); //所属投资人
		orderEntity.setPublisherBaseAccount(product.getPublisherBaseAccount()); //所属发行人
		orderEntity.setProduct(product); //所属产品
		orderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_redeem));
		orderEntity.setOrderAmount(req.getOrderAmount());
		orderEntity.setOrderVolume(req.getOrderAmount().divide(product.getNetUnitShare()));
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_submitted);
		orderEntity.setCheckStatus(InvestorTradeOrderEntity.TRADEORDER_checkStatus_no);
		orderEntity.setOrderTime(DateUtil.getSqlCurrentDate());
		orderEntity.setCreateTime(DateUtil.getSqlCurrentDate());//取应用服务器时间，为了和noPayInvest订单的createTime相差1s
		return investorTradeOrderService.saveEntity(orderEntity);
	}
	
	private InvestorTradeOrderEntity createRedeemTradeOrder(SpecialRedeemOrderReq req) {
		Product product = this.productService.findByOid(req.getProductOid());
		InvestorTradeOrderEntity orderEntity = new InvestorTradeOrderEntity();
		InvestorBaseAccountEntity baseAccount = this.investorBaseAccountService.findByUid(req.getUid());
		orderEntity.setInvestorBaseAccount(baseAccount); //所属投资人
		orderEntity.setPublisherBaseAccount(product.getPublisherBaseAccount()); //所属发行人
		orderEntity.setProduct(product); //所属产品
		orderEntity.setOrderCode(this.seqGenerator.next(CodeConstants.special_redeem));
		orderEntity.setOrderAmount(req.getOrderAmount());
		orderEntity.setOrderVolume(req.getOrderAmount().divide(product.getNetUnitShare()).setScale(4,BigDecimal.ROUND_HALF_UP));
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_submitted);
		orderEntity.setOrderTime(DateUtil.getSqlCurrentDate());
		return investorTradeOrderService.saveEntity(orderEntity);
	}
	
	/**
	 * 只有普通赎回normalRedeem、清盘赎回clearRedeem 需要调支付接口打钱给用户。
	 * 对于还本付息cash、募集失败退款cashFailed、活转定--赎回 noPayRedeem、定转活--赎回 noPayRedeem
	 * cash,cashFailed直接扣定期份额，增活期份额
	 * noPayRedeem 直接扣活期份额，增定期份额
	 */
	private void redeemPay(InvestorTradeOrderEntity orderEntity) {
		RedeemPayRequest req = generateBasicRedeemPayRequest(orderEntity);
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType()) 
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_clearRedeem.equals(orderEntity.getOrderType()) ) {
			// 支付
			this.paymentServiceImpl.redeemPay(req);
		} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem.equals(orderEntity.getOrderType())) {
			
			// 支付
			this.paymentServiceImpl.specialRedeemPay(req);
		} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			// 支付
			this.paymentServiceImpl.incrementRedeemPay(req);
		}
	}
	
	private RedeemPayRequest generateBasicRedeemPayRequest(InvestorTradeOrderEntity orderEntity) {
		RedeemPayRequest req = new RedeemPayRequest();
		req.setUserOid(orderEntity.getInvestorBaseAccount().getMemberId());
		req.setOrderNo(orderEntity.getOrderCode());
		req.setAmount(orderEntity.getOrderAmount());
		req.setFee(BigDecimal.ZERO);
		req.setProvince(orderEntity.getProvince());
		req.setCity(orderEntity.getCity());
		req.setOrderTime(DateUtil.format(orderEntity.getOrderTime(), DateUtil.fullDatePattern));
		req.setPayDate(DateUtil.format(orderEntity.getPayDate(), DateUtil.datePattern));
		return req;
	}
	
	/**
	 * 赎回直接回调
	 * 普通赎回 normalRedeem 需要等支付回调
	 * 对于 还本付息cash、募集失败退款cashFailed、活转定--赎回noPayRedeem，定转活--赎回noPayRedeem
	 * cash、cashFailed、noPayRedeem 自己回调自己成功
	 */
	public void selfNotify(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_cashFailed.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderEntity.getOrderType())) {
			OrderNotifyReq ireq = new OrderNotifyReq();
			ireq.setOrderCode(orderEntity.getOrderCode());
			ireq.setReturnCode(PayParam.ReturnCode.RC0000.toString());
			redeemCallback(ireq);
		}
	}

	public void plusSelfNotify(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem.equals(orderEntity.getOrderType())) {
			OrderNotifyReq ireq = new OrderNotifyReq();
			ireq.setOrderCode(orderEntity.getOrderCode());
			ireq.setReturnCode(PayParam.ReturnCode.RC0000.toString());
            plusRedeemCallback(ireq);
		}
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public boolean redeemCallback(OrderNotifyReq ireq) {
		
		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(ireq.getOrderCode());
		String orderStatus = InvestorTradeOrderEntity.TRADEORDER_orderStatus_done;
		if (PayParam.ReturnCode.RC0000.toString().equals(ireq.getReturnCode())) {
			if(orderEntity.getOrderType().equals(InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem)){
				InvestorSpecialRedeemEntity specalRedeem = this.investorSpecialRedeemService.findByUserId(orderEntity.getInvestorBaseAccount().getUserOid());
				// 判断剩余份额
				if(specalRedeem.getLeftSpecialRedeemAmount().compareTo(orderEntity.getOrderAmount())>0){
					this.updateSpecialRedeemAmount(orderEntity.getInvestorBaseAccount().getUserOid(), orderEntity.getOrderAmount());
				}else if(specalRedeem.getLeftSpecialRedeemAmount().compareTo(orderEntity.getOrderAmount())<=0){
					this.updateSpecialRedeemAmount(orderEntity.getInvestorBaseAccount().getUserOid(), specalRedeem.getLeftSpecialRedeemAmount());
					this.investorTradeOrderService.updateBaseAccountStatus(orderEntity.getInvestorBaseAccount().getUserOid());
				}
				// 判断用户状态是否可以更新为正常
//				if(specalRedeem.getLeftSpecialRedeemAmount().compareTo(SysConstant.BIGDECIMAL_defaultValue)==0){
//					//更新用户账户为正常
//				}
			}
			/** 创建<<投资人-资金变动明细>> */
			this.investorCashFlowService.createCashFlow(orderEntity);
			
			/** 账户系统事件发送 */
			setAccSysInvestEvent(orderEntity);

			/** 推广平台事件发送 */
			this.tulipService.tulipEventDeal(orderEntity);
			
			/** 补单事件发送 */
			sendReEvent(orderEntity);
			
			if(InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
				sendMessage(orderEntity, DealMessageEnum.REDEEM_DONE.name());
			}
		}
		orderEntity.setOrderStatus(orderStatus);
		orderEntity.setCompleteTime(DateUtil.getSqlCurrentDate());
		this.investorTradeOrderService.saveEntity(orderEntity);
		
		return true;
	}

    @Transactional(value = TxType.REQUIRES_NEW)
    public boolean plusRedeemCallback(OrderNotifyReq ireq) {
        InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(ireq.getOrderCode());
        String orderStatus = InvestorTradeOrderEntity.TRADEORDER_orderStatus_done;
        if (PayParam.ReturnCode.RC0000.toString().equals(ireq.getReturnCode())) {
            /** 创建<<投资人-资金变动明细>> */
            this.investorCashFlowService.createCashFlow(orderEntity);

            /** 推广平台事件发送 */
            this.tulipService.tulipEventDeal(orderEntity);

            /** 补单事件发送 */
            sendReEvent(orderEntity);
        }
        orderEntity.setOrderStatus(orderStatus);
        orderEntity.setCompleteTime(DateUtil.getSqlCurrentDate());
        this.investorTradeOrderService.saveEntity(orderEntity);

        InvestorBfPlusRedeemEntity investorBfPlusRedeemEntity = investorBfPlusRedeemService.getByOid(orderEntity.getOid());
        this.bfPlusRedeemDone(investorBfPlusRedeemEntity.getBaseAmount(), orderEntity.getInvestorBaseAccount().getOid(), orderEntity.getProduct().getOid(), PublisherHoldEntity.PUBLISHER_HOLD_HOLD_STATUS_holding);

        // 更新T_MONEY_INVESTOR_PLUS_REDEEM状态
        investorBfPlusRedeemService.updateBfPlusRedeem(1, orderEntity.getOid());
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

	private void sendReEvent(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_reRedeem.equals(orderEntity.getOrderType())) {
			String originalOrderCode = abandonLogService.getOriginalOrderCodeByRefundOrderCode(orderEntity.getOrderCode());
			platformFinanceCompareDataResultNewService.updateDealStatusDealtByOrderCode(originalOrderCode);
			modifyOrderNewService.updateDealStatusDealtByOrderCode(originalOrderCode);
		}
	}

	private void setAccSysInvestEvent(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			TradeRequest ireq = new TradeRequest();
			ireq.setUserOid(orderEntity.getInvestorBaseAccount().getMemberId());
			ireq.setUserType(AccParam.UserType.INVESTOR.toString());
			ireq.setOrderType(AccParam.OrderType.REDEEM.toString());
			ireq.setRelationProductNo(orderEntity.getProduct().getOid());
			ireq.setBalance(orderEntity.getOrderAmount());
			ireq.setRemark("investor redeem");
			ireq.setOrderNo(orderEntity.getOrderCode());
			accmentService.writeLog(ireq);
		}
	}

    public int bfPlusRedeemDone(BigDecimal volume, String investorOid, String productOid, String holdStatus){
        return this.publisherHoldDao.bfPlusRedeemDone(volume, investorOid, productOid, holdStatus);
    }
}
