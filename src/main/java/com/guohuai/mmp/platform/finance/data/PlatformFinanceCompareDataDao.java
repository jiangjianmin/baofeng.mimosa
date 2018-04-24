package com.guohuai.mmp.platform.finance.data;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlatformFinanceCompareDataDao extends JpaRepository<PlatformFinanceCompareDataEntity, String>, JpaSpecificationExecutor<PlatformFinanceCompareDataEntity>{
	
	@Query(value = "DELETE FROM T_MONEY_CHECK_COMPAREDATA "
			+ " WHERE buzzDate = ?1", nativeQuery = true)
	@Modifying
	public void deleteByBuzzDate(Date buzzDate);
	
	
	public PlatformFinanceCompareDataEntity findByOrderCode(String orderCode);
	
	@Query(value = "SELECT * FROM T_MONEY_CHECK_COMPAREDATA "
			+ " WHERE buzzDate = ?1 AND orderStatus != 5 AND checkStatus = 'no' ", nativeQuery = true)
	public List<PlatformFinanceCompareDataEntity> findByCheckDateAndCheckStatus(String checkDate);
}
