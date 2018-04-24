package com.guohuai.mmp.job.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface JobLogDao extends JpaRepository<JobLogEntity, String>, JpaSpecificationExecutor<JobLogEntity> {

	
	@Query(value = "select count(*) from  T_MONEY_JOB_LOG where jobId = ?1 and batchStartTime >= ?2 and batchEndTime <= ?3 ", nativeQuery = true)
	int queryRunedTimes(String jobId, String begin, String end);
	
	



}
