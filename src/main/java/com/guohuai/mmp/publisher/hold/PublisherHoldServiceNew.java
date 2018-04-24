package com.guohuai.mmp.publisher.hold;


import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;

/**
 * 持有人手册_内部事务类.
 * 
 * @author Jeffrey Wong
 *
 */
@Service
@Transactional
public class PublisherHoldServiceNew {

	private static final Logger logger = LoggerFactory.getLogger(PublisherHoldServiceNew.class);

	@Autowired
	private PublisherHoldDao publisherHoldDao;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private CacheHoldService cacheHoldService;

	/**
	 * 根据分仓更新合仓可赎回份额
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void unlockRedeemItem(InvestorTradeOrderEntity orderEntity) {
		
		

//		if (InvestorCouponOrderEntity.TYPE_COUPON_TASTECOUPON.equals(orderEntity.getCouponType())) {
//			
//		} else {
			int i = this.publisherHoldDao.unlockRedeem(orderEntity.getPublisherHold().getOid(), orderEntity.getHoldVolume());
			if (i < 1) {
				logger.info("========解锁可赎回根据分仓更新合仓可赎回份额" + orderEntity.getOid() + "处理失败");
				return;
			}
			this.investorTradeOrderService.unlockRedeem(orderEntity.getOid());
//			cacheHoldService.syncHoldRedeemableVolume(orderEntity);
//		}
		
		

	}
	
	/**
	 * 根据分仓更新合仓可计息份额
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void unlockAccrualItem(InvestorTradeOrderEntity entity) {
		int i = this.publisherHoldDao.unlockAccrual(entity.getPublisherHold().getOid(), entity.getHoldVolume());
		if (i < 1) {
			logger.info("========根据分仓更新合仓可计息份额" + entity.getOid() + "处理失败");
			return;
		}
		this.investorTradeOrderService.unlockAccrual(entity.getOid());
//		cacheHoldService.syncHoldAccruableHoldVolume(entity);
		
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public void dealHold(String oid) {
		PublisherHoldEntity hold = this.publisherHoldDao.findOne(oid);
		BigDecimal value = BigDecimal.ZERO;
		BigDecimal totalVolume = BigDecimal.ZERO;
		BigDecimal holdVolume = BigDecimal.ZERO;
		BigDecimal redeemableHoldVolume = BigDecimal.ZERO;
		
		BigDecimal lockRedeemHoldVolume = BigDecimal.ZERO;
		BigDecimal accruableHoldVolume = BigDecimal.ZERO;
		
		
		
		BigDecimal holdTotalIncome = BigDecimal.ZERO;
		BigDecimal totalBaseIncome = BigDecimal.ZERO;
		
		List<InvestorTradeOrderEntity> orderList = this.investorTradeOrderService.findByPublisherHold(hold);
		for (InvestorTradeOrderEntity orderEntity : orderList) {
			totalVolume = totalVolume.add(orderEntity.getHoldVolume());
			holdVolume = holdVolume.add(orderEntity.getHoldVolume());
			if (orderEntity.getRedeemStatus().equals(InvestorTradeOrderEntity.TRADEORDER_redeemStatus_no)) {
				lockRedeemHoldVolume = lockRedeemHoldVolume.add(orderEntity.getHoldVolume());
			} else {
				redeemableHoldVolume = redeemableHoldVolume.add(orderEntity.getHoldVolume());
			}
			if (orderEntity.getAccrualStatus().equals(InvestorTradeOrderEntity.TRADEORDER_accrualStatus_yes)) {
				accruableHoldVolume = accruableHoldVolume.add(orderEntity.getHoldVolume());
			}
			holdTotalIncome = holdTotalIncome.add(orderEntity.getTotalIncome());
			totalBaseIncome = totalBaseIncome.add(orderEntity.getTotalBaseIncome());
		}
		value = totalVolume;
		
		hold.setValue(value);
		hold.setTotalVolume(totalVolume);
		hold.setHoldVolume(holdVolume);
		hold.setRedeemableHoldVolume(redeemableHoldVolume);
		hold.setLockRedeemHoldVolume(lockRedeemHoldVolume);
		hold.setAccruableHoldVolume(accruableHoldVolume);
		hold.setHoldTotalIncome(holdTotalIncome);
		hold.setTotalBaseIncome(totalBaseIncome);
		this.publisherHoldDao.save(hold);
	}
	
	
}
