package com.guohuai.mmp.platform.reserved.couponcashdetails;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountService;
import com.guohuai.mmp.platform.reserved.order.ReservedOrderService;
import com.guohuai.mmp.platform.tulip.TulipService;
import com.guohuai.mmp.tulip.req.TulipVerificationReq;

@Service
@Transactional
public class CouponCashDetailsService {

	Logger logger = LoggerFactory.getLogger(CouponCashDetailsService.class);

	@Autowired
	private CouponCashDetailsDao couponCashDetailsDao;
	@Autowired
	private PlatformBaseAccountService platformBaseAccountService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private TulipService tulipNewService;
	@Autowired
	private ReservedOrderService reservedOrderService;

	public CouponCashDetailsEntity createEntity(BigDecimal couponAmount, String coupons) {
		CouponCashDetailsEntity en = new CouponCashDetailsEntity();
		en.setPlatformBaseAccount(platformBaseAccountService.getPlatfromBaseAccount());
		en.setCashAmount(couponAmount);
		en.setCoupons(coupons);
		en.setCashStatus(CouponCashDetailsEntity.DETAIL_cashStatus_toCash);
		
		return this.couponCashDetailsDao.save(en);
	}
	
	public void cancelDo(List<String> vs) {
		for (String v : vs) {
			CouponCashDetailsEntity en = this.couponCashDetailsDao.findByCoupons(v);
			if (null == en) {
				throw AMPException.getException("卡券不存在");
			}
			this.couponCashDetailsDao.updateCashStatus(v, CouponCashDetailsEntity.DETAIL_cashStatus_cashed);
		}
		
		 
	}
	
	
	public void cancellation() {
		String lastOid = "0";
		BigDecimal orderAmount = BigDecimal.ZERO;
		while (true) {
			List<CouponCashDetailsEntity> list = this.couponCashDetailsDao.findByCashStatus(CouponCashDetailsEntity.DETAIL_cashStatus_toCash, lastOid);
			if (list.isEmpty()) {
				break;
			}
			
			for (CouponCashDetailsEntity en : list) {
				
				InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByCoupons(en.getCoupons());
				
				try {
					TulipVerificationReq req = new TulipVerificationReq();
					req.setOrderCode(orderEntity.getOrderCode());
					req.setOrderAmount(orderEntity.getOrderAmount());
					req.setPayAmount(orderEntity.getPayAmount());
					req.setCouponId(orderEntity.getCoupons());
					req.setCouponAmount(orderEntity.getCouponAmount());
					req.setProOid(orderEntity.getProduct().getOid());
					req.setProductName(orderEntity.getProduct().getName());
					
					tulipNewService.verificationCouponForLiquidation(req);
					
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
				}
				orderAmount = orderAmount.add(en.getCashAmount());
				en.setCashStatus(CouponCashDetailsEntity.DETAIL_cashStatus_cashing);
				lastOid = en.getOid();
			}
			couponCashDetailsDao.save(list);
		}
		reservedOrderService.createCancellation(orderAmount);
	}
	
	

}
