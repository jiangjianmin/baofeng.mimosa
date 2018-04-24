package com.guohuai.ams.duration.assetPool.chargefee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;

public interface AssetPoolFeeSettingDao extends JpaRepository<AssetPoolFeeSetting, String>, JpaSpecificationExecutor<AssetPoolFeeSetting> {

	public void deleteByAssetPool(AssetPoolEntity assetPool);
	
	@Query("from AssetPoolFeeSetting s where s.assetPool = ?1 order by startAmount asc")
	public List<AssetPoolFeeSetting> findByAssetPool(AssetPoolEntity assetPool);
	
	@Query("from AssetPoolFeeSetting s where s.assetPool.oid = ?1 order by startAmount asc")
	public List<AssetPoolFeeSetting> findByAssetPoolOid(String assetPoolOid);
	
}
