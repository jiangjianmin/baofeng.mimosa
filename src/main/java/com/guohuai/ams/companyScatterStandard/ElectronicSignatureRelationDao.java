package com.guohuai.ams.companyScatterStandard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ElectronicSignatureRelationDao extends JpaRepository<ElectronicSignatureRelation, String>, JpaSpecificationExecutor<ElectronicSignatureRelation> {

	@Query(value = "select * from t_gam_tradeorder_electronicsignatrue_relation t1 WHERE t1.productOid = ?1 and t1.electronicSignatureUrl is null  ", nativeQuery = true)
	List<ElectronicSignatureRelation> getElectronicSignatureRelationByProductOid(String productOid);

}
