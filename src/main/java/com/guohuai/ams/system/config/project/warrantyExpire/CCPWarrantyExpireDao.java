package com.guohuai.ams.system.config.project.warrantyExpire;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CCPWarrantyExpireDao extends JpaRepository<CCPWarrantyExpire, String> {

	@Query("from CCPWarrantyExpire w order by w.weight asc")
	public List<CCPWarrantyExpire> search();
	
}
