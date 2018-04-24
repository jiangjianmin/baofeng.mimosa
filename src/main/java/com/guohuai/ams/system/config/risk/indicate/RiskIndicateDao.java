package com.guohuai.ams.system.config.risk.indicate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface RiskIndicateDao extends JpaRepository<RiskIndicate, String>, JpaSpecificationExecutor<RiskIndicate> {

	@Query("from RiskIndicate i where i.cate.type like ?1 and (i.title like ?2 or i.cate.title like ?2) and i.state in ?3 order by i.cate.type asc, i.cate.title asc, i.title asc")
	public List<RiskIndicate> search(String type, String keyword, String[] state);
	
	@Query("from RiskIndicate i where i.cate.type = ?1 and i.state in ?2 order by i.cate.title asc, i.title asc")
	public List<RiskIndicate> search(String type, String[] state);
	
	@Query("from RiskIndicate i where i.state in ?1 order by i.cate.type asc, i.cate.title asc, i.title asc")
	public List<RiskIndicate> search(String[] state);
	
	@Query("from RiskIndicate i where i.state in ?1 and i.cate.oid in ?2 order by i.cate.type asc, i.cate.title asc, i.title asc")
	public List<RiskIndicate> search(String[] state, String[] coid);

}
