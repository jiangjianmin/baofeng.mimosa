package com.guohuai.ams.duration.capital;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CapitalDao extends JpaRepository<CapitalEntity, String>, JpaSpecificationExecutor<CapitalEntity> {

	@Query("from CapitalEntity a where a.assetPoolOid = ?1")
	public Page<CapitalEntity> findByOid(String oid, Pageable pageable);
}
