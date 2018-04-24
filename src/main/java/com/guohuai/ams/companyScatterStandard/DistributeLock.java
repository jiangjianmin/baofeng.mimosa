package com.guohuai.ams.companyScatterStandard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 *
 *分布式锁
 * @author yujianlong
 * @date 2018/4/21 18:42
 * @param
 * @return
 */
@Slf4j
public class DistributeLock {
	private static final String KEYPREFIX="c:g:a:c:distributelock:";
	private static final Charset UTF8CHARSET=Charset.forName("utf8");
	private RedisTemplate<String, String> redis;
	private volatile boolean locked = false;
	private String lockKey;
	private List<String> lockKeys;
	private int randomStart=99;
	private int randomEnd=200;
	 /**
	  * 锁多长时间算过期
     * 锁超时时间，防止线程在入锁以后，无限的执行等待
     */
    private int expireMsecs = 60 * 1000;

    /**
     * 获取不到锁，需要等待多长时间10s
     * 
     */
    private int timeoutMsecs = 10 * 1000;
    
    public DistributeLock() {
    	
    }
	public DistributeLock(RedisTemplate<String, String> redis, String lockKey) {
		this.redis = redis;
		this.lockKey = KEYPREFIX+lockKey+"_lock";
	}
	public DistributeLock(RedisTemplate<String, String> redis, String lockKey, int timeoutMsecs) {
		this(redis, lockKey);
		this.timeoutMsecs = timeoutMsecs;
	}
	public DistributeLock(RedisTemplate<String, String> redis, String lockKey, int expireMsecs, int timeoutMsecs) {
		this(redis, lockKey, timeoutMsecs);
		this.expireMsecs = expireMsecs;
	}
	public DistributeLock(RedisTemplate<String, String> redis, String lockKey, int expireMsecs, int timeoutMsecs, int randomStart, int randomEnd) {
		this(redis, lockKey, timeoutMsecs);
		this.expireMsecs = expireMsecs;
		this.randomStart = randomStart;
		this.randomEnd = randomEnd;
	}
	public String getLockKey() {
		return lockKey;
	}
	public void setLockKey(String lockKey) {
		this.lockKey = lockKey;
	}
	public int getExpireMsecs() {
		return expireMsecs;
	}
	public void setExpireMsecs(int expireMsecs) {
		this.expireMsecs = expireMsecs;
	}
	public int getTimeoutMsecs() {
		return timeoutMsecs;
	}
	public void setTimeoutMsecs(int timeoutMsecs) {
		this.timeoutMsecs = timeoutMsecs;
	}
	
	/**
	 * 获取锁
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean lock() {
		 int timeout = timeoutMsecs;
		 while (timeout >= 0) {
			 long expires = System.currentTimeMillis() + expireMsecs + 1;
			 String expiresStr = String.valueOf(expires);
			 if (setNX(lockKey, expiresStr)) {
				 locked = true;
				 log.debug("{}获得锁",lockKey);
	             return true;
			 }
			//setnx失败了
			 String currentValueStr =get(lockKey);
			 //锁存在，但是过期了
			 if(currentValueStr!=null&&
			 			Long.parseLong(currentValueStr)<System.currentTimeMillis()
				){
				 String oldValueStr =getSet(lockKey, expiresStr);//获取旧的值，设置新的值
				 if(oldValueStr!=null&&oldValueStr.equals(currentValueStr)){ //getset成功了
					 locked = true;
					 log.debug("{}获得锁",lockKey);
	                 return true;
				 }
			 }
			 long randomWaitTime=ThreadLocalRandom.current().nextInt(randomStart, randomEnd);
			 timeout -= randomWaitTime;
			 
			 try {
				TimeUnit.MILLISECONDS.sleep(randomWaitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		 log.debug("{}未获得锁",lockKey);
		return false;
	}
	
	
	
	/**
	 * 释放锁
	 */
	public synchronized void unLock(){
		if (locked) {
			log.debug("{}释放锁",lockKey);
			del(lockKey);
			locked=false;
		}
	}
	
	public static void main(String[] args) {
//		DistributeLock lock=new DistributeLock(redis, lockKey, expireMsecs, timeoutMsecs);
//		try {
//			if (lock.lock()) {
//				
//			}
//		} finally {
//			// TODO: handle finally clause
//			lock.unLock();
//		}
	}
	
	/**
	 * setNX操作
	 * @param key
	 * @param value
	 * @return
	 */
	private boolean setNX(String key,String value){
		log.debug("执行redis SET NX操作  {} {}", key, value);
		Object obj = null;
		try {
			obj= redis.execute(connection->{
				StringRedisSerializer serializer = new StringRedisSerializer();
				Boolean flag=connection.setNX(serializer.serialize(key), serializer.serialize(value));
				return flag;
			}, true, false);
		} catch (Exception e) {
			log.error("执行redis SET NX操作失败  {} {}", key, value);
			return false;
		}
		return obj != null?(Boolean) obj:false;
	}
	
	
	/**
	 * 删除key
	 * @param key
	 * @return
	 */
	private Long del(String key){
		log.debug("执行redis dek操作  {}", key);
		Long num=0L;
		try {
			num= redis.execute(connection->{
				StringRedisSerializer serializer = new StringRedisSerializer();
				Long result=connection.del(serializer.serialize(key));
				return result;
			}, true, false);
		} catch (Exception e) {
			log.error("执行redis dek操作失败  {}", key);
			return num;
		}
		return num;
		
	}
	
	/**
	 * 获取key
	 * @param key
	 * @return
	 */
	private String get(String key){
		log.debug("执行redis get操作成功  {}", key);
		Object obj=null;
		try {
			obj=redis.execute(connection->{
				StringRedisSerializer serializer = new StringRedisSerializer();
				byte[] data = connection.get(serializer.serialize(key));
                if (data == null) {
                    return null;
                }
                return serializer.deserialize(data);
			}, true);
		} catch (Exception e) {
			log.error("执行redis get操作失败  {}", key);
			return null;
		}
		
		return obj != null ? obj.toString() : null;
		
	}
	private String getSet(String key,String value){
		log.debug("执行redis getset操作成功  key:{} value:{}", key,value);
		Object obj=null;
		try {
			obj=redis.execute(connection->{
				StringRedisSerializer serializer = new StringRedisSerializer();
				byte[] data = connection.getSet(serializer.serialize(key),serializer.serialize(value));
				return serializer.deserialize(data);
			}, true);
		} catch (Exception e) {
			log.debug("执行redis getset操作失败  key:{} value:{}", key,value);
			return null;
		}
		
		return obj != null ? obj.toString() : null;
		
	}
	
	
	
	
	
}
