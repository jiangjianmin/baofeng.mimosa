package com.guohuai.ams.product.reward;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductIncomeRewardSnapshotDao extends JpaRepository<ProductIncomeRewardSnapshot, String>, JpaSpecificationExecutor<ProductIncomeRewardSnapshot> {

	@Query(value = "from ProductIncomeRewardSnapshot pir where pir.snapshotDate=?1 order by pir.startDate")
	public List<ProductIncomeRewardSnapshot> findBySnapshotDate(Date snapshotDate);

	@Modifying
	@Query(value = "insert into T_GAM_INCOME_REWARD_SNAPSHOT(oid,productOid,level,startDate,endDate,ratio,dratio,snapshotDate) select REPLACE(uuid(), '-', ''),productOid,level,startDate,endDate,ratio,dratio,?2 from T_GAM_INCOME_REWARD where productOid=?1", nativeQuery = true)
	int snapshotProductIncomeReward(String productOid, Date incomeDate);
	
	@Query(value = "select count(1) from T_GAM_INCOME_REWARD_SNAPSHOT where productOid=?1 and snapshotDate=?2", nativeQuery = true)
	public int countByProductOidAndSnapshotDate(String productOid, Date snapshotDate);
}
