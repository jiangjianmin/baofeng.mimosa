package com.guohuai.ams.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductLogDao extends JpaRepository<ProductLog, String>, JpaSpecificationExecutor<ProductLog> {
	
	@Query(value = "SELECT * FROM `T_GAM_PRODUCT_LOG` a WHERE a.`productOid` = ?1 AND a.`auditType` = ?2 AND a.`auditState` = ?3 LIMIT 1 ", nativeQuery = true)
	public ProductLog findProductLogsByCon(String productOid, String auditType, String auditState);
	
}
