package com.guohuai.mmp.investor.baseaccount.log;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CouponLogDao extends JpaRepository<CouponLogEntity, String>, JpaSpecificationExecutor<CouponLogEntity> {
	@Query(value = "SELECT * from T_MONEY_COUPON_LOG "
			+ "WHERE sendedTimes < limitSendTimes AND nextNotifyTime < sysdate()"
			+ " AND status='FAILED' LIMIT 200", nativeQuery = true)
	List<CouponLogEntity> getCouponLogEntity();
}
