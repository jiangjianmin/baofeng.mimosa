package com.guohuai.ams.duration.assetPool.scope;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ScopeDao extends JpaRepository<ScopeEntity, String>, JpaSpecificationExecutor<ScopeEntity>  {

	@Query("from ScopeEntity a where a.assetPool.oid = ?1")
	public List<ScopeEntity> findByAssetPoolOid(String assetPoolOid);
	
	@Query(value = "delete from T_GAM_ASSETPOOL_INVEST_SCOPE where assetPoolOid = ?1", nativeQuery = true)
	@Modifying
	public void deleteByPid(String pid);
}
