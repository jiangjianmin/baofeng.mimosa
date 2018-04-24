package com.guohuai.ams.duration.assetPool.history;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HistoryValuationDao extends JpaRepository<HistoryValuationEntity, String>, JpaSpecificationExecutor<HistoryValuationEntity> {

	public List<HistoryValuationEntity> findByAssetPoolOidAndBaseDate(String assetPoolOid, Date baseDate);

}
