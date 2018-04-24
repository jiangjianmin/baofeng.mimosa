package com.guohuai.ams.system.config.project.warrantyMode;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CCPWarrantyModeDao extends JpaRepository<CCPWarrantyMode, String> {
	
	@Query("from CCPWarrantyMode w order by w.weight asc")
	public List<CCPWarrantyMode> search();

}
