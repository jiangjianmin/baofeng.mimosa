package com.guohuai.ams.system.config.risk.warning;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;


public interface RiskWarningDao extends JpaRepository<RiskWarning, String>, JpaSpecificationExecutor<RiskWarning> {
	public void deleteByIndicate(RiskIndicate indicate);

	@Modifying
	@Query("delete from RiskWarning o where o.indicate.oid = ?1 ")
	public void deleteByIndicate(String warning);

	@Query("from RiskWarning i where i.indicate = ?1 order by i.indicate.cate.type asc, i.indicate.cate.oid asc, i.indicate.oid asc, i.oid asc")
	public List<RiskWarning> search(RiskIndicate indicate);

	@Query("from RiskWarning i where i.indicate.cate.type like ?1 and (i.title like ?2 or i.indicate.title like ?2 or i.indicate.cate.title like ?2) and i.indicate.state in ?3 order by i.indicate.cate.type asc, i.indicate.cate.oid asc, i.indicate.oid asc, i.oid asc")
	public List<RiskWarning> search(String type, String keyword, String[] state);

	@Query("from RiskWarning i where i.indicate.cate.type = ?1 and i.indicate.state in ?2 order by i.indicate.cate.oid asc, i.indicate.oid asc, i.oid asc")
	public List<RiskWarning> search(String type, String[] state);

	@Query("from RiskWarning i where i.indicate.state in ?1 order by i.indicate.cate.type asc, i.indicate.cate.oid asc, i.indicate.oid asc, i.oid asc")
	public List<RiskWarning> search(String[] state);

	@Query("from RiskWarning i where i.indicate.state in ?1 and i.indicate.oid in ?2 order by i.indicate.cate.type asc, i.indicate.cate.oid asc, i.indicate.oid asc, i.oid asc")
	public List<RiskWarning> search(String[] state, String[] ioid);

	@Query("from RiskWarning i where i.indicate.oid in ?1 order by i.indicate.cate.type asc, i.indicate.cate.oid asc, i.indicate.oid asc, i.oid asc")
	public List<RiskWarning> findByIndicateOid(String[] ioid);

	@Query("from RiskWarning i where i.indicate.oid = ?1 order by i.indicate.cate.type asc, i.indicate.cate.oid asc, i.indicate.oid asc, i.oid asc")
	public List<RiskWarning> findByIndicateOid(String ioid);
}
