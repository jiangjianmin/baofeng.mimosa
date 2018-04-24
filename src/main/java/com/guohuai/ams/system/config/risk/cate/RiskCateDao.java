package com.guohuai.ams.system.config.risk.cate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface RiskCateDao extends JpaRepository<RiskCate, String>,JpaSpecificationExecutor<RiskCate> {

	@Query("from RiskCate c where c.type = ?1 order by c.title asc")
	public List<RiskCate> search(String type);
	
	@Query("from RiskCate c order by c.type asc, c.title asc")
	public List<RiskCate> search();
	

}
