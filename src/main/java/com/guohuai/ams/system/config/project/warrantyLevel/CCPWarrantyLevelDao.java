package com.guohuai.ams.system.config.project.warrantyLevel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CCPWarrantyLevelDao extends JpaRepository<CCPWarrantyLevel, String> {

	@Query("from CCPWarrantyLevel w order by w.oid asc")
	public List<CCPWarrantyLevel> search();
	
}
