package com.guohuai.mmp.investor.tradeorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvestorTradeOrderCouponService { 
	@Autowired
	private InvestorTradeOrderCouponDao tradeOrderCouponDao;
	
	public void saveTradeOrderCoupon(InvestorTradeOrderCouponEntity entity) {
		tradeOrderCouponDao.save(entity);
	}
}
