package com.guohuai.mmp.publisher.baseaccount.statistics.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PublisherStatisticsHistoryDao extends JpaRepository<PublisherStatisticsHistoryEntity, String>,
		JpaSpecificationExecutor<PublisherStatisticsHistoryEntity> {

	@Query(value = " DELETE FROM T_MONEY_PUBLISHER_STATISTICS_HISTORY WHERE confirmDate=?1 ", nativeQuery = true)
	@Modifying
	int deleteByConfirmDate(java.sql.Date confirmDate);
	
}
