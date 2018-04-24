package com.guohuai.cache.service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.guohuai.basic.component.ext.web.PageResp;
import com.guohuai.cache.dao.RedisExecuteLogDao;
import com.guohuai.cache.entity.RedisExecuteLogEntity;
import com.guohuai.cache.entity.RedisExecuteLogPojo;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.GHUtil;
import com.guohuai.component.util.HashRedisUtil;
import com.guohuai.component.util.ZsetRedisUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisExecuteLogExtService {
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private RedisExecuteLogService redisExecuteLogService;
	@Autowired
	private RedisExecuteLogDao redisExecuteLogDao;
	
	private RedisExecuteLogEntity saveRedisExecuteLogEntity(String command,String batchNo, String hkey) {
		return this.saveRedisExecuteLogEntity(command, batchNo, hkey, null, null, null);
	}
	
	private RedisExecuteLogEntity saveRedisExecuteLogEntity(String command,String batchNo,String hkey,String key,String value,String backValue){
		RedisExecuteLogEntity redisExecuteLogEntity = new RedisExecuteLogEntity();
		redisExecuteLogEntity.setBatchNo(batchNo);
		redisExecuteLogEntity.setHkey(hkey);
		redisExecuteLogEntity.setField(key);
		redisExecuteLogEntity.setExecuteCommand(command);
		if(command.equals("HMSET")){
			redisExecuteLogEntity.setErrorCommand("DEL");
		}else if(command.equals("HSET")){
			redisExecuteLogEntity.setErrorCommand("HSET");
		}else{
			redisExecuteLogEntity.setErrorCommand(command);
		}
		redisExecuteLogEntity.setValue(value);
		redisExecuteLogEntity.setBackValue(backValue);
		
		redisExecuteLogEntity.setExecuteSuccessStatus(RedisExecuteLogEntity.EXECUTE_STATUS_FAILED);
		redisExecuteLogEntity.setExecuteFailedStatus(RedisExecuteLogEntity.EXECUTE_STATUS_FAILED);
		redisExecuteLogEntity.setErrorCount(0);
		redisExecuteLogEntity.setExecuteTime(DateUtil.getSqlCurrentDate());
		return redisExecuteLogEntity;
	}
	
	
	/**
	 * redis执行HINCR命令
	 */
	public BigDecimal hincrByBigDecimal(String hkey, String key, BigDecimal value,
			Object backValue) {
		Long valueIn = 0L;
		if (value instanceof BigDecimal) {
			value = DecimalUtil.zoomOut((BigDecimal)value);
			valueIn = ((BigDecimal) value).longValue();
		} 

		log.info("==command:{} {} {} {}", RedisExecuteLogEntity.HINCRBYFLOAT, hkey, key, valueIn);
		BigDecimal valOut = HashRedisUtil.hincrByFloat(redis, hkey, key, valueIn);
		log.info("==valOut:{}", valOut);
		valOut = DecimalUtil.zoomIn(valOut);
		return valOut;
	}
	
	public Long hincrByLong(String hkey, String key, Object value,
			Object backValue) {
		Integer valueIn = 0;
		valueIn = ((Integer) value).intValue();
		log.info("==command:{} {} {} {}", RedisExecuteLogEntity.HINCRBY, hkey, key, valueIn);
		Long valOut = HashRedisUtil.hincrByLong(redis, hkey, key, valueIn);
		log.info("==valOut:{}", valOut);
		
		return valOut;
	}
	
	/**
	 *  redis执行HMSET命令
	 */
	public void redisExecuteHMSET(String batchNo, String hkey, Object obj) {
		DecimalUtil.zoomOut(obj);
		RedisExecuteLogEntity redisLogEntity = saveRedisExecuteLogEntity(RedisExecuteLogEntity.HMSET, batchNo, hkey);
		log.info("==command:{} {} {} ", RedisExecuteLogEntity.HMSET, batchNo, hkey);
		HashRedisUtil.hmset(redis, hkey, GHUtil.obj2Map(obj));
		redisLogEntity.setExecuteSuccessStatus(RedisExecuteLogEntity.EXECUTE_STATUS_SUCCESS);
		this.redisExecuteLogService.save(redisLogEntity);
	}
	
	
	public void zadd(String batchNo, String zkey, String value, long score) {
		RedisExecuteLogEntity redisLogEntity = saveRedisExecuteLogEntity(RedisExecuteLogEntity.ZADD, batchNo, zkey, null,
				value.toString(), null);
		ZsetRedisUtil.zAdd(redis, zkey, value, score);
		log.info("==command:{} {} {} {}", RedisExecuteLogEntity.ZADD, zkey, value, score);
		redisLogEntity.setExecuteSuccessStatus(RedisExecuteLogEntity.EXECUTE_STATUS_SUCCESS);
		this.redisExecuteLogService.save(redisLogEntity);
	}
	
	public PageResp<RedisExecuteLogPojo> findRedisExecuteLogEntity(Specification<RedisExecuteLogEntity> spec,
			Pageable pageable) {

		Page<RedisExecuteLogEntity> list = this.redisExecuteLogDao.findAll(spec, pageable);
		PageResp<RedisExecuteLogPojo> pageResp = new PageResp<RedisExecuteLogPojo>();
		for (RedisExecuteLogEntity entity : list) {
			RedisExecuteLogPojo rep = new RedisExecuteLogPojo();
			rep.setValue(entity.getValue());
			rep.setHkey(entity.getHkey());
			rep.setBatchNo(entity.getBatchNo());
			rep.setOid(entity.getOid());
			rep.setField(entity.getField());
			rep.setBackValue(entity.getBackValue());
			rep.setErrorCommand(entity.getErrorCommand());
			rep.setErrorCount(entity.getErrorCount());
			rep.setExecuteCommand(entity.getErrorCommand());
			rep.setExecuteFailedStatus(entity.getExecuteFailedStatus());
			rep.setExecuteSuccessStatus(entity.getExecuteSuccessStatus());
			rep.setExecuteTime(entity.getExecuteTime());
			rep.setCreateTime(entity.getCreateTime());
			pageResp.getRows().add(rep);
		}
		pageResp.setTotal(list.getTotalElements());
		return pageResp;
	
	}

	public long getExpire(String key) {
		return redis.getExpire(key);
	}
	
	public void expire(String key, long timeout) {
		redis.expire(key, timeout, TimeUnit.SECONDS);
	}
}
