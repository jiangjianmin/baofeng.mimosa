package com.guohuai.mmp.job.lock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface JobLockDao extends JpaRepository<JobLockEntity, String>, JpaSpecificationExecutor<JobLockEntity> {


//	JobLockEntity findByBatchCodeAndJobId(String batchCode, String jobId);
	
	@Query(value = "update JobLockEntity set jobStatus = 'processing' where jobId = ?1 and jobStatus = 'toRun' ")
	@Modifying
	int updateStatus4Lock(String jobId);

	JobLockEntity findByJobId(String jobId);
	
	@Query(value = "update JobLockEntity set jobStatus = 'toRun' where jobId = ?1 and jobStatus = 'processing' ")
	@Modifying
	int resetJob(String jobId);
	
	



}
