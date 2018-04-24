package com.guohuai.ams.duration.target;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RepaymentScheduleDao
		extends JpaRepository<RepaymentScheduleEntity, String>, JpaSpecificationExecutor<RepaymentScheduleEntity> {

	@Query("from RepaymentScheduleEntity a where a.assetPoolOid = ?1 and a.repaymentDate >= CURDATE() order by a.targetOid")
	public Page<RepaymentScheduleEntity> findByPid(String pid, Pageable pageable);
	
	@Query(value = "update T_GAM_ASSETPOOL_REPAYMENT set status = '已支付'"
			+ " where holdOid = ?1 and seq = ?2", nativeQuery = true)
	@Modifying
	public void update(String holdOid, Integer seq);
	
	@Query(value = "delete from T_GAM_ASSETPOOL_REPAYMENT"
			+ " where targetOid = ?1", nativeQuery = true)
	@Modifying
	public void delete(String targetOid);
}
