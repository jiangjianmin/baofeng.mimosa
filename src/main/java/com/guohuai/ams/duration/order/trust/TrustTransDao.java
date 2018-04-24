package com.guohuai.ams.duration.order.trust;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TrustTransDao extends JpaRepository<TrustTransEntity, String>, JpaSpecificationExecutor<TrustTransEntity> {

	@Query(value = "SELECT b.* FROM T_GAM_ASSETPOOL_TARGET a"
	  		+ " LEFT JOIN T_GAM_ASSETPOOL_TARGET_TRANS b ON a.oid = b.targetOid"
	  		+ " WHERE a.assetPoolOid = ?1 and b.state not in ('-1', '31')", nativeQuery = true)
	public List<TrustTransEntity> findTransByPidForAppointment(String pid);
	
	@Query(value = "SELECT b.* FROM T_GAM_ASSETPOOL_TARGET a"
	  		+ " LEFT JOIN T_GAM_ASSETPOOL_TARGET_TRANS b ON a.oid = b.targetOid"
	  		+ " WHERE a.assetPoolOid = ?1 and b.state = 2", nativeQuery = true)
	public List<TrustTransEntity> findTransByPidForConfirm(String pid);
	
	@Query(value = "update T_GAM_ASSETPOOL_TARGET_TRANS set state = '-1', operator = ?2 where oid = ?1", nativeQuery = true)
	@Modifying
	public void updateOrder(String oid, String operator);
	
	@Query(value = "SELECT a.* FROM T_GAM_ASSETPOOL_TARGET_TRANS a"
			+ " LEFT JOIN T_GAM_ASSETPOOL_TARGET b "
			+ " ON a.targetOid = b.oid"
			+ " WHERE b.assetPoolOid = ?1 AND a.confirmDate = ?2", nativeQuery = true)
	public List<TrustTransEntity> findByPidAndConfirmDate(String pid, Date date);
}
