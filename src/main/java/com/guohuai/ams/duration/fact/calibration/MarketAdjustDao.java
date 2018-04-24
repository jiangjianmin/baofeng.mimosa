package com.guohuai.ams.duration.fact.calibration;

import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;

public interface MarketAdjustDao extends JpaRepository<MarketAdjustEntity, String>, JpaSpecificationExecutor<MarketAdjustEntity> {

	@Query("from MarketAdjustEntity a where a.assetPool.oid = ?1 and a.status not in ('delete', 'init')")
	public Page<MarketAdjustEntity> getListByPid(String pid, Pageable pageable);

	@Query(value = "select * from T_GAM_ASSETPOOL_MARKET_ADJUST where assetPoolOid = ?1 and status = 'pass'" + " order by baseDate desc limit 1", nativeQuery = true)
	public MarketAdjustEntity getMaxBaseDateByPid(String pid);

	@Query("from MarketAdjustEntity a where a.assetPool.oid = ?1 and  a.baseDate = ?2 and a.status in ('create', 'pass')")
	public MarketAdjustEntity findByBaseDate(String pid, Date baseDate);

	@Query("from MarketAdjustEntity a where a.assetPool.oid = ?1 and a.status = 'pass' order by a.baseDate asc")
	public List<MarketAdjustEntity> getListForYield(String pid);

	public MarketAdjustEntity findByAssetPoolAndBaseDate(AssetPoolEntity assetPool, Date baseDate);
}
