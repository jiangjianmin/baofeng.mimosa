package com.guohuai.ams.duration.order.trust;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TrustOrderDao extends JpaRepository<TrustOrderEntity, String>, JpaSpecificationExecutor<TrustOrderEntity> {

	@Query("from TrustOrderEntity a where a.assetPoolOid = ?1 and a.state not in ('-1', '31')")
	public List<TrustOrderEntity> findPurchaseByPidForAppointment(String pid);
	
	@Query("from TrustOrderEntity a where a.assetPoolOid = ?1 and a.state = 2")
	public List<TrustOrderEntity> findPurchaseByPidForConfirm(String pid);
	
	@Query(value = "update T_GAM_ASSETPOOL_TARGET_ORDER set state = '-1', operator = ?2 where oid = ?1", nativeQuery = true)
	@Modifying
	public void updateOrder(String oid, String operator);
}
