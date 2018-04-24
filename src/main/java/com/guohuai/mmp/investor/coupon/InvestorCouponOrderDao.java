package com.guohuai.mmp.investor.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvestorCouponOrderDao extends JpaRepository<InvestorCouponOrderEntity, String>,
		JpaSpecificationExecutor<InvestorCouponOrderEntity> {

	public InvestorCouponOrderEntity findByOrderCode(String orderCode);

}
