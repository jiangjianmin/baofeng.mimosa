package com.guohuai.ams.system.config.risk.options;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;

public interface RiskOptionsDao extends JpaRepository<RiskOptions, String> {

	public void deleteByIndicate(RiskIndicate indicate);

	@Query("from RiskOptions o where o.indicate.cate.type = ?1 and (o.indicate.title like ?2 or o.indicate.cate.title like ?2) order by o.indicate.cate.title asc, o.indicate.title asc, o.seq asc")
	public List<RiskOptions> search(String type, String keyword);

	@Query("from RiskOptions o where o.indicate.cate.type = ?1 order by o.indicate.cate.title asc, o.indicate.title asc, o.seq asc")
	public List<RiskOptions> search(String type);

	@Query("from RiskOptions o where o.indicate = ?1 order by o.seq asc")
	public List<RiskOptions> search(RiskIndicate indicate);

}
