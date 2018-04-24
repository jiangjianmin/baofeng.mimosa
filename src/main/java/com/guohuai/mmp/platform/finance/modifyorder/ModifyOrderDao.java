package com.guohuai.mmp.platform.finance.modifyorder;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface ModifyOrderDao  extends JpaRepository<ModifyOrderEntity, String>, JpaSpecificationExecutor<ModifyOrderEntity>{
	
	@Query(value = "UPDATE T_MONEY_PLATFORM_FINANCE_MODIFYORDER "
			+ "SET approveStatus=?2,"
			+ "operator=?3 "
			+ "where oid=?1", nativeQuery = true)
	@Modifying
	public int modifyOrderApprove(String oid, String approveStatus, String operator);
	
	@Query(value = "DELETE FROM T_MONEY_PLATFORM_FINANCE_MODIFYORDER "
			+ " WHERE checkOid=?1", nativeQuery = true)
	@Modifying
	public void deleteByCheckOid(String checkOid);
	
	@Query(value = "UPDATE T_MONEY_PLATFORM_FINANCE_MODIFYORDER "
			+ "SET dealStatus='dealt' "
			+ "where orderCode=?1 AND dealStatus='dealing'", nativeQuery = true)
	@Modifying
	public int updateDealStatusDealtByOrderCode(String orderCode);

	@Query(value = "UPDATE T_MONEY_PLATFORM_FINANCE_MODIFYORDER "
			+ "SET dealStatus='dealing' "
			+ "where orderCode=?1 AND dealStatus='toDeal'", nativeQuery = true)
	@Modifying
	public int updateDealStatusDealingByOrderCode(String orderCode);
	
	@Query(value = "SELECT * FROM T_MONEY_PLATFORM_FINANCE_MODIFYORDER WHERE oid IN ?1 ", nativeQuery = true)
	public List<ModifyOrderEntity> findModifyOrderByOids(List<String> oids);

}
