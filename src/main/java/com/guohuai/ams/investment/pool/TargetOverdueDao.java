package com.guohuai.ams.investment.pool;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface TargetOverdueDao
		extends JpaRepository<TargetOverdue, String>, JpaSpecificationExecutor<TargetOverdue> {
	
	/**
	 * 根据投资标的id查询逾期信息
	 * @Title: findByTargetOid 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param targetOid
	 * @return TargetOverdue    返回类型
	 */
	@Query("from TargetOverdue to where to.investment.oid = ?1")
	public TargetOverdue findByTargetOid(String targetOid);
}
