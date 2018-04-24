package com.guohuai.ams.duration.fact.income.schedule;


import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface IncomeScheduleDao extends JpaRepository<IncomeSchedule, String>, JpaSpecificationExecutor<IncomeSchedule> {

	public IncomeSchedule findByAssetPoolOidAndBasicDate(String assetPoolOid, Date basicDate);

	// 将当前日期处于待审核状态的数据置为已失效
	@Query(value = "update T_GAM_ASSETPOOL_INCOMESCHEDULE_SCHEDULING set status = 'lose' where status = 'toApprove' and basicDate <= ?1", nativeQuery = true)
	@Modifying
	public void changeLose(Date basicDate);

	// 将当前日期处于待审核状态的数据置为已失效
	@Query(value = "from IncomeSchedule a where a.status='pass' and a.basicDate = ?1")
	public List<IncomeSchedule> findToActive(Date basicDate);

	// 获取当前该资产池已排期最大日期
	@Query(value = "select max(basicDate) from T_GAM_ASSETPOOL_INCOMESCHEDULE_SCHEDULING where assetpoolOid = ?1", nativeQuery = true)
	public Date getShouldBaseDate(String assetpoolOid);

}
