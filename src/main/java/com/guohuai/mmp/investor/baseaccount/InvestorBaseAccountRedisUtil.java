package com.guohuai.mmp.investor.baseaccount;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSONObject;

public class InvestorBaseAccountRedisUtil {
	
	private static final Charset UTF8CHARSET=Charset.forName("utf8");
	
	public static final String USER_INFO_REDIS_KEY = "c:g:u:ai:";
	
	private static final Logger logger = LoggerFactory.getLogger(InvestorBaseAccountRedisUtil.class);
		
	public static InvestorBaseAccountRedisInfo set(RedisTemplate<String, String> redis, final String key, final InvestorBaseAccountRedisInfo value){
		final String json = JSONObject.toJSONString(value);
		return redis.execute(new RedisCallback<InvestorBaseAccountRedisInfo>() {

			@Override
			public InvestorBaseAccountRedisInfo doInRedis(RedisConnection connection)
					throws DataAccessException {
				logger.info("user.redis={},val={}", key, json);
				connection.set((USER_INFO_REDIS_KEY + key).getBytes(UTF8CHARSET), json.getBytes(UTF8CHARSET));
				return value;
			}
		});
	}
	
	public static InvestorBaseAccountRedisInfo get(RedisTemplate<String, String> redis, final String key){
		return redis.execute(new RedisCallback<InvestorBaseAccountRedisInfo>() {
			@Override
			public InvestorBaseAccountRedisInfo doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte [] data = connection.get((USER_INFO_REDIS_KEY + key).getBytes(UTF8CHARSET));
				if (data == null) {
					return null;
				}
				return JSONObject.parseObject(new String(data, UTF8CHARSET), InvestorBaseAccountRedisInfo.class);
			}
		});
	}
}
