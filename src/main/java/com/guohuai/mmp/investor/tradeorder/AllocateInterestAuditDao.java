package com.guohuai.mmp.investor.tradeorder;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AllocateInterestAuditDao extends JpaRepository<AllocateInterestAudit, String>, JpaSpecificationExecutor<AllocateInterestAudit> {
	
	/**
	 * 审核
	 */
	@Query("update AllocateInterestAudit set auditor = ?2, auditStatus = ?3, auditTime = ?4, auditComment = ?5, updateTime = ?4 where oid = ?1 and auditStatus='TOAUDIT' ")
	@Modifying
	public int updateAllocateInterestAudit(String oid, String auditor, String AuditStatus, Timestamp now, String auditComment);
	
	/**
	 * 查询产品待审核的派息申请
	 */
	@Query(value = "select * from T_GAM_ALLOCATE_INTEREST_AUDIT where productOid = ?1 and auditStatus in('TOAUDIT','AUDITPASS') ", nativeQuery = true)
	public List<AllocateInterestAudit> findInterestToAudit(String productOid);
	
	@Query(value = "select * from T_GAM_ALLOCATE_INTEREST_AUDIT where productOid = ?1 and auditStatus = 'TOAUDIT' ", nativeQuery = true)
	public AllocateInterestAudit findProductInterestToAudit(String productOid);
}
