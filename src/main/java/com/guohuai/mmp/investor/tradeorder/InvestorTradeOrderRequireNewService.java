package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.mmp.platform.accment.AccParam;
import com.guohuai.mmp.platform.accment.AccmentService;
import com.guohuai.mmp.platform.accment.TradeRequest;
import com.guohuai.mmp.publisher.hold.PublisherHoldDao;

//import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
//@Slf4j
public class InvestorTradeOrderRequireNewService {
	
	@Autowired
	private PublisherHoldDao publisherHoldDao;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private AccmentService accmentService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void processOneItem(InvestorTradeOrderEntity orderInEntity) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderInEntity.getOrderCode());
		InvestorTradeOrderEntity redeemEntity = investorRedeemTradeOrderService.createExpRedeemTradeOrder(orderEntity);
		/** 解锁体验金金额，增加可赎回金额 */
		int i = this.publisherHoldDao.unlockExpGoldVolume(orderEntity.getPublisherHold().getOid(), 
				orderEntity.getOrderVolume(), orderEntity.getTotalIncome());
		if (i < 1) {
			throw new AMPException("体验金分合仓平仓异常");
		}
		
		this.publisherHoldDao.flatExpGoldVolume(orderEntity.getPublisherHold().getOid(), orderEntity.getOrderVolume(), 
				orderEntity.getProduct().getNetUnitShare());
		this.flatExpGoldVolume(orderEntity.getOid(), orderEntity.getOrderVolume());
		
		TradeRequest tradeRequest = new TradeRequest();
		tradeRequest.setOrderType(AccParam.OrderType.EXPEXPIRED.toString());
		tradeRequest.setBalance(redeemEntity.getOrderAmount());
		tradeRequest.setRelationProductNo(redeemEntity.getProduct().getOid());
		tradeRequest.setUserOid(redeemEntity.getInvestorBaseAccount().getMemberId());
		tradeRequest.setUserType(AccParam.UserType.INVESTOR.toString());
		tradeRequest.setOrderNo(redeemEntity.getOrderCode());
		tradeRequest.setRemark("TasteCoupon Over");
		accmentService.writeLog(tradeRequest);
	}
	
	public int flatExpGoldVolume(String oid, BigDecimal orderVolume) {
		int i = this.investorTradeOrderDao.flatExpGoldVolume(oid, orderVolume);
		if (i < 0) {
			throw new AMPException("体验金分仓平仓异常");
		}
		return i;
	}

}
