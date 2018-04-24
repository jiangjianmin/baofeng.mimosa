package com.guohuai.ams.duration.order.trust;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TrustDao extends JpaRepository<TrustEntity, String>, JpaSpecificationExecutor<TrustEntity> {

	@Query(value = "SELECT b.oid, b.name, a.investVolume FROM T_GAM_ASSETPOOL_TARGET a"
			+ " LEFT JOIN T_GAM_ASSETPOOL b ON a.assetPoolOid = b.oid"
			+ " WHERE a.targetOid = ?1 LIMIT ?2, ?3", nativeQuery = true)
	public List<Object> findByTargetOid(String targetOid, int page, int rows);
	
	@Query(value = "SELECT count(*) FROM T_GAM_ASSETPOOL_TARGET a"
			+ " LEFT JOIN T_GAM_ASSETPOOL b ON a.assetPoolOid = b.oid"
			+ " WHERE a.targetOid = ?1", nativeQuery = true)
	public int findCountByTargetOid(String targetOid);
	
	@Query("from TrustEntity a where a.assetPoolOid = ?1 and a.state in ('0', '1')")
	public Page<TrustEntity> findByPidForConfirm(String assetPoolOid, Pageable pageable);
	
	@Query("from TrustEntity a where a.assetPoolOid = ?1 and a.state = '0'")
	public List<TrustEntity> findFundListByPid(String assetPoolOid);
	
	@Query(value = "SELECT assetPoolOid, SUM(investVolume) AS investVolume"
			+ " FROM T_GAM_ASSETPOOL_TARGET"
			+ " WHERE targetOid = ?1 AND state = '0'"
			+ " GROUP BY assetPoolOid, targetOid", nativeQuery = true)
	public List<Object> findByTargetOidForObj(String targetOid);
	
	public List<TrustEntity> findByTargetOid(String targetOid);
	
	@Query(value = "update T_GAM_ASSETPOOL_TARGET set state = '-1' where oid = ?1", nativeQuery = true)
	@Modifying
	public void updateOrder(String oid);
}
