package com.guohuai.cache.service;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.guohuai.cache.dao.RedisExecuteLogDao;
import com.guohuai.cache.entity.RedisExecuteLogEntity;
@Service
@Transactional
public class RedisExecuteLogService {
	@Autowired
	RedisExecuteLogDao redisExecuteLogDao;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public RedisExecuteLogEntity save(RedisExecuteLogEntity redisExecuteLogEntity){
		return this.redisExecuteLogDao.save(redisExecuteLogEntity);
	}
	public List<RedisExecuteLogEntity> findRedisExecuteLogByBatchNo(String batchNo){
		return redisExecuteLogDao.findRedisExecuteLogByBatchNo(batchNo);
	}
	public List<RedisExecuteLogEntity> findRedisExecuteLogExecuteFailed() {
		return redisExecuteLogDao.findRedisExecuteLogExecuteFailed();
	}
	 
}
