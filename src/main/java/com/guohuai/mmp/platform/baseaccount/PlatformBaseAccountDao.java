package com.guohuai.mmp.platform.baseaccount;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlatformBaseAccountDao extends JpaRepository<PlatformBaseAccountEntity, String>, JpaSpecificationExecutor<PlatformBaseAccountEntity> {

	PlatformBaseAccountEntity findByStatus(String platformbaseAccountstatusNormal);
	
	@Query(value = "update PlatformBaseAccountEntity set balance = balance + ?2 where oid = ?1 and status = 'normal'")
	@Modifying
	int syncBalance(String oid, BigDecimal orderAmount);
	
	@Query(value = "update PlatformBaseAccountEntity set balance = balance + ?2 where status = 'normal'")
	@Modifying
	int updateBalancePlusPlus(BigDecimal orderAmount);
	
	@Query(value = "update PlatformBaseAccountEntity set balance = balance - ?2 where status = 'normal' and balance >= ?1 ")
	@Modifying
	int updateBalanceMinusMinus(BigDecimal orderAmount);
	
	@Query(value = "update PlatformBaseAccountEntity set balance = balance - ?1, superAccBorrowAmount = superAccBorrowAmount + ?1 where status = 'normal' and balance >= ?1")
	@Modifying
	int payToSuperman(BigDecimal amount);
	
	@Query(value = "update T_MONEY_PLATFORM_BASEACCOUNT set balance = balance + ?1,"
			+ " superAccBorrowAmount = if(superAccBorrowAmount <= ?1, 0, superAccBorrowAmount - ?1) where status = 'normal'", nativeQuery = true)
	@Modifying
	int borrowFromSuperman(BigDecimal amount);

	

	

}
