package com.guohuai.component.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSON;
import com.guohuai.component.web.view.BaseReq;

public class ListRedisUtil {
	private static final Charset UTF8CHARSET = Charset.forName("utf8");

	public static boolean lpush(RedisTemplate<String, String> redis, final String key, final Object... value) {
		return redis.execute(new RedisCallback<Boolean>() {

			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				List<byte[]> list = new ArrayList<byte[]>();
				for (Object object : value) {
					if (object instanceof BaseReq) {
						list.add(JSON.toJSONString(object).getBytes(UTF8CHARSET));
					}else{
						list.add(object.toString().getBytes(UTF8CHARSET));
					}
					
				}
				byte[][] arr = list.toArray(new byte[list.size()][0]);
				connection.lPush(key.getBytes(UTF8CHARSET), arr);
				return true;
			}

		});
	}
	
	public static boolean lpushLimit(RedisTemplate<String, String> redis, final String key, final long limit, final Object... value) {
		return redis.execute(new RedisCallback<Boolean>() {

			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				List<byte[]> list = new ArrayList<byte[]>();
				for (Object object : value) {
					if (object instanceof BaseReq) {
						list.add(JSON.toJSONString(object).getBytes(UTF8CHARSET));
					}else{
						list.add(object.toString().getBytes(UTF8CHARSET));
					}
				}
				byte[][] arr = list.toArray(new byte[list.size()][0]);
				connection.lPush(key.getBytes(UTF8CHARSET), arr);
				connection.lTrim(key.getBytes(UTF8CHARSET), 0,limit-1);
				return true;
			}

		});
	}
}
