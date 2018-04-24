package com.guohuai.ams.companyScatterStandard;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *分布式锁的工具类
 *
 * @author yujianlong
 * @date 2018/4/21 18:43
 * @param
 * @return
 */
public class DistributeLockUtil {
	
	public static <T,R> R handleWithLock(DistributeLock lock,T t,Function<T, R> funtion){
		try {
			if (lock.lock()) {
				return funtion.apply(t);
			}
		} finally {
			lock.unLock();
		}
		return null;
	}
	
	public static boolean tryLockWithList(List<String> tList,RedisTemplate<String, String> redis,String lockPrefix){
		List<DistributeLock> allDistributeLock=tList.parallelStream().map(lockKey->new DistributeLock(redis, lockPrefix+lockKey+"_lock", 60000, 10000))
		.collect(Collectors.toList());
		
		return allDistributeLock.parallelStream().map(DistributeLock::lock).allMatch(t->t==true);
	}
	
	
	public static <T,R> R handleWithLock(List<T> tList,Function<T, R> funtion){
		return null;
	}

}
