package com.guohuai.mmp.investor.tradeorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface InvestorTradeOrderCouponDao extends JpaRepository<InvestorTradeOrderCouponEntity, String>, JpaSpecificationExecutor<InvestorTradeOrderCouponEntity>{ 
	
}
