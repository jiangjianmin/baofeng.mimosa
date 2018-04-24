package com.guohuai.ams.duration.fact.income.schedule;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface IncomeScheduleApplyDao extends JpaRepository<IncomeScheduleApply, String>, JpaSpecificationExecutor<IncomeScheduleApply> {

	IncomeScheduleApply findByAssetPoolOidAndBasicDateAndStatus(String assetpoolOid, Date basicDate,
			String statusToapprove);

	// 将当前日期处于待审核状态的数据置为已失效
	@Query(value = "update T_GAM_ASSETPOOL_INCOMESCHEDULE_APPLY set status = 'lose' where status = 'toApprove' and basicDate <= ?1", nativeQuery = true)
	@Modifying
	void changeLose(Date basicDate);


}
