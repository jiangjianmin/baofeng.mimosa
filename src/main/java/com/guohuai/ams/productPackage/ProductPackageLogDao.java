package com.guohuai.ams.productPackage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductPackageLogDao extends JpaRepository<ProductPackageLog, String>, JpaSpecificationExecutor<ProductPackageLog> {
	
	@Query(value = "SELECT * FROM `T_GAM_PRODUCT_LOG` a WHERE a.`productOid` = ?1 AND a.`auditType` = ?2 AND a.`auditState` = ?3 LIMIT 1 ", nativeQuery = true)
	public ProductPackageLog findProductPackageLogsByCon(String productOid, String auditType, String auditState);
	
}
