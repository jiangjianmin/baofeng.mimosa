package com.guohuai.mmp.platform.baseaccount.statistics.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlatformStatisticsHistoryDao extends JpaRepository<PlatformStatisticsHistoryEntity, String>,
		JpaSpecificationExecutor<PlatformStatisticsHistoryEntity> {

	@Query(value = " DELETE FROM T_MONEY_PLATFORM_STATISTICS_HISTORY WHERE confirmDate=?1 ", nativeQuery = true)
	@Modifying
	int deleteByConfirmDate(java.sql.Date confirmDate);
}
