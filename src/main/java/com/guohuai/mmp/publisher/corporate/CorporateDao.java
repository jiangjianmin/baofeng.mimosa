package com.guohuai.mmp.publisher.corporate;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CorporateDao extends JpaRepository<Corporate, String>, JpaSpecificationExecutor<Corporate> {

	@Query("from Corporate c where c.account like %?1% and c.createTime between ?2 and ?3 order by c.createTime desc")
	public List<Corporate> findByAccountAndCreateTime(String account, Timestamp beginTime, Timestamp endTime);

	@Query("from Corporate c where c.account like %?1% and c.createTime between ?2 and ?3 and c.status = ?4 order by c.createTime desc")
	public List<Corporate> findByAccountAndCreateTimeAndStatus(String account, Timestamp beginTime, Timestamp endTime, int status);

	public Corporate findByAccount(String account);

	public Corporate findByAuditOrderNo(String auditOrderNo);

}
