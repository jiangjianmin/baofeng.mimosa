package com.guohuai.component.util;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;


public class GHUtil {
	private static Logger log=LoggerFactory.getLogger(GHUtil.class);
	private static final Charset utf8 = Charset.forName("utf8");
	/**
	 * 将对象转换成map，并过滤掉为空的属性
	 * @param req 需要转换的对象
	 * @return 传入对象属性和值形成的map
	 */
	
	public static Map<String, Object> obj2Map(Object req) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<FieldInfo> fields = TypeUtils.computeGetters(req.getClass(), null);
		for (FieldInfo f : fields) {
			try {
				Object v = f.getMethod().invoke(req);

				if (v != null) {
					map.put(f.getName(), v);
				}

			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
		}
		log.debug("obj2Map,originObj={},map={},obj={}", req, map, JSON.toJSONString(map));
		return map;
	}
	
	/**
	 * 将一个对象有值的属性增加上应的值，并存储到redis中
	 * @param hmMap 需要批量incr的键值对
	 * @param key redis 中的key 
	 * @param redis
	 * @return
	 */
	public static List<Object> hmIncrBy(Map<String,String> hmMap,String key,RedisTemplate<String, String> redis){
		return redis.executePipelined(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				for (Iterator<String> i = hmMap.keySet().iterator(); i.hasNext();) {
					String k = (String) i.next();
					Object v=hmMap.get(k);
					connection.execute("HINCRBY", key.getBytes(utf8), k.getBytes(utf8),
							v.toString().getBytes(utf8));
				}
				return null;
			}
		}) ;
	}
	
	/**
	 * 生成序列号
	 * @param redis
	 * @param prefix 前缀
	 * @return
	 */
	public static String getSeqNo(RedisTemplate<String, String> redis, final String prefix) {
		Calendar cal = Calendar.getInstance();
		String key = String.format("%s%s%02d%02d", prefix, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH + 1),
				cal.get(Calendar.DAY_OF_MONTH));
		long v = redis.opsForValue().increment(key, 1);
		return String.format("%s%08d", key, v);
	}
	
	public static void put(Map<String, Object> map, Object v, String k) {
		if (!StringUtils.isEmpty(v)) {
			map.put(k, v);
		}
	}
}
