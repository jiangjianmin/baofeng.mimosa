package com.guohuai.ams.system.config.risk.warning.options;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.system.config.risk.warning.RiskWarning;

public interface RiskWarningOptionsDao extends JpaRepository<RiskWarningOptions, String>, JpaSpecificationExecutor<RiskWarningOptions> {

	public void deleteByWarning(RiskWarning warning);
	
	@Modifying
	@Query("delete from RiskWarningOptions o where o.warning.oid = ?1 ")
	public void deleteByWarning(String warning);
	
	@Modifying
	@Query("delete from RiskWarningOptions o where o.warning.indicate.oid = ?1 ")
	public void deleteByIndicate(String warning);

	@Query("from RiskWarningOptions o where o.warning.indicate.cate.type = ?1 and (o.warning.title like ?2 or o.warning.indicate.title like ?2 or o.warning.indicate.cate.title like ?2) order by o.warning.indicate.cate.oid asc,o.warning.indicate.oid asc, o.warning.oid asc, o.seq asc")
	public List<RiskWarningOptions> search(String type, String keyword);

	@Query("from RiskWarningOptions o where o.warning.indicate.cate.type = ?1 order by o.warning.indicate.cate.oid asc,o.warning.indicate.oid asc, o.warning.oid asc, o.seq asc")
	public List<RiskWarningOptions> search(String type);

	@Query("from RiskWarningOptions o where o.warning = ?1 order by o.seq asc")
	public List<RiskWarningOptions> search(RiskWarning warning);
}
