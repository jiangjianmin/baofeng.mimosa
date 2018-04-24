package com.guohuai.moonBox.util;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

public class SetRedisUtil {
	
	private static final Charset UTF8CHARSET = Charset.forName("utf8");
	
	public static final String FAMILY_PLAN_KEY_PRE="m:f:p:";
	public static final String FAMILY_PLAN_COUNT_KEY_PRE="investCount";
	public static final String FAMILY_PLAN_STATUS_KEY_PRE="investStatus";
	public static final String FAMILY_PLAN_REMARK_KEY_PRE="investMsg";
	public static final String CHANGE_PHONENUM_FAIL_COUNT_Y="c:p:fc:y:";//用户修改手机号原手机号可用失败次数
	public static final String CHANGE_PHONENUM_FAIL_COUNT_N="c:p:fc:n:";//用户修改手机号原手机号不可用失败次数
	public static final String CHANGE_PHONENUM_USER_LOCK_Y="c:p:lock:y:";//禁止用户进行手机号修改
	public static final String CHANGE_PHONENUM_USER_LOCK_N="c:p:lock:n:";//禁止用户进行手机号修改
	public static final String CHANGE_PHONENUM_USER_INFO="c:p:u:i:";//用户变更信息
	public static final String CHANGE_PHONENUM_PAYPASSWOED="payPwd";//支付密码
	public static final String CHANGE_PHONENUM_REALNAME="realName";//用户姓名
	public static final String CHANGE_PHONENUM_IDCARENO="idCardNo";//用户身份证号
	private final static Long GQTIME=7200L;//2小时
	public static Boolean hSet(RedisTemplate<String, String> redis, final String Hashkey,final String key,final String value) {
		return redis.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				Boolean bytes = connection.hSet(Hashkey.getBytes(UTF8CHARSET), key.getBytes(UTF8CHARSET), value.getBytes(UTF8CHARSET));
				return bytes;
			}
		});
	}
	public static String hGet(RedisTemplate<String, String> redis, final String Hashkey,final String key) {
		return redis.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] bytes = connection.hGet(Hashkey.getBytes(UTF8CHARSET), key.getBytes(UTF8CHARSET));
				return new String(bytes, UTF8CHARSET);
			}
		});
	}
	public static Boolean hExists(RedisTemplate<String, String> redis, final String Hashkey,final String key){
		return redis.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				Boolean bytes = connection.hExists(Hashkey.getBytes(UTF8CHARSET), key.getBytes(UTF8CHARSET));
				return bytes;
			}
		});
	}
	public static Boolean exists(RedisTemplate<String, String> redis,final String key){
		return redis.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				Boolean bytes = connection.exists(key.getBytes(UTF8CHARSET));
				return bytes;
			}
		});
	}
	public static Long hIncrBy(RedisTemplate<String, String> redis, final String Hashkey,final String key,final Long value){
		return redis.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				Long bytes = connection.hIncrBy(Hashkey.getBytes(UTF8CHARSET), key.getBytes(UTF8CHARSET), value);
				return bytes;
			}
		});
	}
	public static Long IncrBy(RedisTemplate<String, String> redis,final String key,final Long value){
		return redis.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				Long bytes = connection.incrBy(key.getBytes(UTF8CHARSET), value);
				return bytes;
			}
		});
	}
	public static String Get(RedisTemplate<String, String> redis,final String key){
		return redis.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] bytes = connection.get(key.getBytes(UTF8CHARSET));
				return new String(bytes, UTF8CHARSET);
			}
		});
	} 
	public static Boolean SetNxOnTimes(RedisTemplate<String, String> redis,final String key,final String value){
		return redis.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				Boolean bytes = connection.setNX(key.getBytes(UTF8CHARSET),value.getBytes(UTF8CHARSET));
				connection.expire(key.getBytes(), GQTIME);
				return bytes;
			}
		});
	} 
	public static Boolean del(RedisTemplate<String, String> redis,final String key){
		return redis.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				Long bytes = connection.del(key.getBytes(UTF8CHARSET));
				if(bytes==1L){
					return true;
				}
				return false;
			}
		});
	} 
	public static Boolean expire(RedisTemplate<String, String> redis, String key, int minutes) {
		return redis.expire(key, minutes, TimeUnit.MINUTES);
	}
}
