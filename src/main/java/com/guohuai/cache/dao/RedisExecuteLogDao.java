package com.guohuai.cache.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.cache.entity.RedisExecuteLogEntity;

public interface RedisExecuteLogDao extends JpaRepository<RedisExecuteLogEntity, String>, JpaSpecificationExecutor<RedisExecuteLogEntity>{
	/**
	 * 查询执行成功的日志，然后做回滚
	 * @param batchNo
	 * @return
	 */
	@Query(value="SELECT * FROM T_MONEY_CACHE_EXECUTE_LOG WHERE batchNo = ?1 and executeSuccessStatus='SUCCESS' ", nativeQuery = true)
	public List<RedisExecuteLogEntity> findRedisExecuteLogByBatchNo(String batchNo);
	
	@Query(value="SELECT * FROM T_MONEY_CACHE_EXECUTE_LOG WHERE executeFailedStatus='FAILED' and executeSuccessStatus='SUCCESS' and errorCount between 1 and 6", nativeQuery = true)
	public List<RedisExecuteLogEntity> findRedisExecuteLogExecuteFailed();
}
