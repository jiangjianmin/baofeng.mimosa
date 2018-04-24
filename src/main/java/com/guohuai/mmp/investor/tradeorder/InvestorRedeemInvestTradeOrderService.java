package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.cache.CacheKeyConstants;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.mmp.serialtask.RedeemInvestParams;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskService;

@Service
@Transactional
public class InvestorRedeemInvestTradeOrderService  {
	

	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private  InvestorInvestTradeOrderService investorInvestTradeOrderService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private MessageSendUtil messageSendUtil;
	@Autowired
	private ProductIncomeRewardCacheService rewardCacheService;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Transactional(value = TxType.REQUIRES_NEW)
	public TradeOrderRep rmInvest(String investOrderCode, BigDecimal ratio, Integer raiseDays,RedeemTradeOrderReq redeemReq) {
		TradeOrderRep rep = new TradeOrderRep();
		
		/**  活期赎回  */
		InvestorTradeOrderEntity redeemOrderEntity = this.investorRedeemTradeOrderService.createNoPayRedeemTradeOrder(redeemReq);
		// 将redeemOrderCode存入Redis，以便异常时修改订单状态 20171017 by wangpeng
		redisTemplate.opsForValue().set(CacheKeyConstants.getRedeemInvestOrder(investOrderCode), redeemOrderEntity.getOrderCode(), 24, TimeUnit.HOURS);
		investorRedeemTradeOrderService.redeemRequiresNew(redeemOrderEntity.getOrderCode());

		InvestorTradeOrderEntity investOrderEntity = this.investorTradeOrderService.findByOrderCode(investOrderCode);
		investOrderEntity.setRatio(ratio);
		investOrderEntity.setRaiseDays(raiseDays);
		investOrderEntity.setRelateOid(redeemOrderEntity.getOid());
		/** 投资相关校验 */
		investorInvestTradeOrderService.invest(investOrderEntity);
		
		RedeemInvestParams params = new RedeemInvestParams();
		params.setInvestOrderCode(investOrderEntity.getOrderCode());
		params.setRedeemOrderCode(redeemOrderEntity.getOrderCode());
		
		SerialTaskReq<RedeemInvestParams> sreq = new SerialTaskReq<RedeemInvestParams>();
		sreq.setTaskParams(params);
		sreq.setTaskCode(SerialTaskEntity.TASK_taskCode_redeemInvest);
		serialTaskService.createSerialTask(sreq);
		
		rep.setOrderStatus(investOrderEntity.getOrderStatus());
		rep.setTradeOrderOid(investOrderEntity.getOid());
		return rep;
	}


	@Transactional(value = TxType.REQUIRES_NEW)
	public void redeemInvestDo(String investorOrderCode, String redeemOrderCode) {
		
		investorRedeemTradeOrderService.redeemDo(redeemOrderCode);
		
		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(investorOrderCode);
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_paySuccess);
		
		this.investorInvestTradeOrderService.investCallBack(orderEntity);
		
		InvestorTradeOrderEntity originEntity = this.investorTradeOrderService.findByOrderCode(redeemOrderCode);
		sendMessage(orderEntity, originEntity);
	}

	public void redeemInvestNoRequireNewDo(String investorOrderCode, String redeemOrderCode) {

		investorRedeemTradeOrderService.redeemDo(redeemOrderCode);

		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(investorOrderCode);
		orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_paySuccess);

		this.investorInvestTradeOrderService.investCallBack(orderEntity);

		InvestorTradeOrderEntity originEntity = this.investorTradeOrderService.findByOrderCode(redeemOrderCode);
		sendMessage(orderEntity, originEntity);
	}
	
	private void sendMessage(InvestorTradeOrderEntity orderEntity, InvestorTradeOrderEntity originEntity) {
		String tag = DealMessageEnum.REPAY_FINISH.name();
		DealMessageEntity messageEntity = new DealMessageEntity();
		messageEntity.setPhone(orderEntity.getInvestorBaseAccount().getPhoneNum());
		messageEntity.setOrderTime(orderEntity.getOrderTime());

		if (Product.TYPE_Producttype_02.equals(orderEntity.getProduct().getType().getOid())) {
			// 定转活有两种情况，流标和还本付息，需要通过relateId关联产品来判断，没有relateId的认为是新手标，没有流标的情况,
			if (StringUtils.isBlank(orderEntity.getRelateOid())
					|| InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(originEntity.getOrderType())) {
				if(rewardCacheService.hasRewardIncome(orderEntity.getProduct().getOid())) {
					messageEntity.setOrderAmount(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
					messageEntity.setProductName(orderEntity.getProduct().getName());
					tag = DealMessageEnum.TRANSFER_DONE.name();
					messageSendUtil.sendTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
				} else {
					messageEntity.setProductName(originEntity.getProduct().getName());
					messageEntity.setTotalMoney(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
					messageSendUtil.sendTimedTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
				}
			}
			// 快定宝赎回
			if (InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem.equals(originEntity.getOrderType())){
				tag = DealMessageEnum.PLUS_REPAY_FINISH.name();
				messageEntity.setProductName(originEntity.getProduct().getName());
				messageEntity.setTotalMoney(orderEntity.getOrderAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
				messageSendUtil.sendTimedTopicMessage(messageSendUtil.getDealTopic(), tag, messageEntity);
			}
		}
	}
}