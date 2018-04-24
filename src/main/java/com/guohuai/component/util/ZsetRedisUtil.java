package com.guohuai.component.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSON;
import com.guohuai.component.web.view.BaseReq;

public class ZsetRedisUtil {
	public static final String SECOND_LEVEL_PROFIT_RANK = "second:level:profit:rank:";
	private static final Charset UTF8CHARSET = Charset.forName("utf8");

	public static Boolean zAdd(RedisTemplate<String, String> redis, final String key, final Object value,final double score) {
		return redis.execute(new RedisCallback<Boolean>() {
			
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				String v;
				if (value instanceof BaseReq) {
					v=JSON.toJSONString(value);
				}else{
					v=value.toString();
				}
				return connection.zAdd(key.getBytes(UTF8CHARSET), score, v.getBytes(UTF8CHARSET));
			}
		});
	}
	
	public static Long zRem(RedisTemplate<String, String> redis, final String key, final Object value) {
		return redis.execute(new RedisCallback<Long>() {
			
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				String v;
				if (value instanceof BaseReq) {
					v=JSON.toJSONString(value);
				}else{
					v=value.toString();
				}
				return connection.zRem(key.getBytes(UTF8CHARSET), v.getBytes(UTF8CHARSET));
			}
		});
	}
	
	public static Long zRemRange(RedisTemplate<String, String> redis, final String key, final int begin, final int end) {
		return redis.execute(new RedisCallback<Long>() {
			
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.zRemRange(key.getBytes(UTF8CHARSET), begin, end);
			}
		});
	}
	
	public static List<String> zRange(RedisTemplate<String, String> redis, final String key, final int begin, final int end) {
		return redis.execute(new RedisCallback<List<String>>() {
			
			@Override
			public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
			
				Set<byte[]> set =  connection.zRange(key.getBytes(UTF8CHARSET), begin, end);
				List<String> list = new ArrayList<String>();
				for (byte[] arr : set) {
					list.add(new String(arr));
				}
				return list;
			}
		});
	}
	
	public static List<String> zRevRange(RedisTemplate<String, String> redis, final String key, final int begin, final int end) {
		return redis.execute(new RedisCallback<List<String>>() {
			
			@Override
			public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
			
				Set<byte[]> set =  connection.zRevRange(key.getBytes(UTF8CHARSET), begin, end);
				List<String> list = new ArrayList<String>();
				for (byte[] arr : set) {
					list.add(new String(arr));
				}
				return list;
			}
		});
	}
}
