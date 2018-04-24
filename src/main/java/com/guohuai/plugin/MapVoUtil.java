package com.guohuai.plugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
/**
 * 
 * @ClassName: MapVoUtil
 * @Description: 原生sql,后期修改成原生sql
 * @author yihonglei
 * @date 2017年5月10日 下午6:40:50
 * @version 1.0.0
 */
public class MapVoUtil {

	/**
	 * 套用的 返回map样式的结果
	 * @param sql
	 * @return
	 */
	public static List<Map<String, Object>> query2Map(String sql,EntityManager em) {
		Query query = em.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = query.getResultList();
		return list;
	}
	public static <T> List<T> query2List(String sql,EntityManager em) {
		Query query = em.createNativeQuery(sql);
		
		List<T> list = query.getResultList();
		return list;
	}
	public static boolean isAllEmpty(Object obj) throws IllegalArgumentException, IllegalAccessException{
		if (null==obj) {
			return true;
		}
		for (Field f : obj.getClass().getDeclaredFields()) {
		    f.setAccessible(true);
		    if (f.get(obj) != null) { //判断字段是否为空，并且对象属性中的基本都会转为对象类型来判断
		        return false;
		    }
		}
		return true;
	}
}
