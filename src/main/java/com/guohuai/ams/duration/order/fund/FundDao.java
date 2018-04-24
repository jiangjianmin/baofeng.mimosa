package com.guohuai.ams.duration.order.fund;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface FundDao extends JpaRepository<FundEntity, String>, JpaSpecificationExecutor<FundEntity> {

	@Query(value = "SELECT b.oid, b.name, a.amount FROM T_GAM_ASSETPOOL_CASHTOOL a"
			+ " LEFT JOIN T_GAM_ASSETPOOL b ON a.assetPoolOid = b.oid"
			+ " WHERE a.cashtoolOid = ?1 LIMIT ?2, ?3", nativeQuery = true)
	public List<Object> findByCashtoolOid(String cid, int page, int rows);
	
	@Query(value = "SELECT count(*) FROM T_GAM_ASSETPOOL_CASHTOOL a"
			+ " LEFT JOIN T_GAM_ASSETPOOL b ON a.assetPoolOid = b.oid"
			+ " WHERE a.cashtoolOid = ?1", nativeQuery = true)
	public int findCountByCashtoolOid(String cid);
	
	@Query("from FundEntity a where a.assetPoolOid = ?1 and a.state = 0")
	public Page<FundEntity> findByPidForConfirm(String assetPoolOid, Pageable pageable);
	
	@Query("from FundEntity a where a.assetPoolOid = ?1 and a.state = 0")
	public List<FundEntity> findFundListByPid(String assetPoolOid);
}
