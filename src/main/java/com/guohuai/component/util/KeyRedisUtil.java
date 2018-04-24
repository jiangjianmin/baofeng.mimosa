package com.guohuai.component.util;

import java.nio.charset.Charset;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

public class KeyRedisUtil {
	private static final Charset UTF8CHARSET = Charset.forName("utf8");

	public static Boolean expire(RedisTemplate<String, String> redis, final String key, final int expire) {
		return redis.execute(new RedisCallback<Boolean>() {

			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.expire(key.getBytes(UTF8CHARSET), expire);
			}

		});
	}
}
