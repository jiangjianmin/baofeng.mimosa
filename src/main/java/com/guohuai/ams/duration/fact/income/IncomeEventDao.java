package com.guohuai.ams.duration.fact.income;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface IncomeEventDao extends JpaRepository<IncomeEvent, String>, JpaSpecificationExecutor<IncomeEvent> {
	
	@Query(value = "SELECT MAX(baseDate) FROM T_GAM_ASSETPOOL_INCOME_ALLOCATE t1, T_GAM_ASSETPOOL_INCOME_EVENT t2 WHERE t1.productOid = ?1 and t1.eventOid = t2.oid AND t2.status = 'ALLOCATED' ", nativeQuery = true)
	Date getLatestIncomeDate(String productOid);

}
