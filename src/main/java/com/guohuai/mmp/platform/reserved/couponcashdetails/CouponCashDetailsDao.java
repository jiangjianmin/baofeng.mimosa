package com.guohuai.mmp.platform.reserved.couponcashdetails;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CouponCashDetailsDao extends JpaRepository<CouponCashDetailsEntity, String>, JpaSpecificationExecutor<CouponCashDetailsEntity> {
	
	
	@Query(value = "select * from T_MONEY_PLATFORM_RESERVED_COUPONCASHDETAILS where cashStatus = ?1 and oid > ?2 order by oid limit 2000", nativeQuery = true)
	List<CouponCashDetailsEntity> findByCashStatus(String cashStatus, String lastOid);
	
	
	@Modifying
	@Query(value = "update CouponCashDetailsEntity set cashStatus = ?2 where coupons = ?2")
	int updateCashStatus(String coupons, String cashStatus);


	CouponCashDetailsEntity findByCoupons(String coupons);



	
	
	

	

}
