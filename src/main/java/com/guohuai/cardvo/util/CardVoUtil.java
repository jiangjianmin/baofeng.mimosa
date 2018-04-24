package com.guohuai.cardvo.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 * 处理返回结果，以及判断对象的工具类
 * 
 * @author yujianlong
 *
 */
public class CardVoUtil {

	/**
	 * 将sql语句查询出来并返回map的list
	 * 
	 * @param sql
	 * @return
	 */
	public static List<Map<String, Object>> query2Map(String sql, EntityManager em) {
		Query query = em.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = query.getResultList();
		list.parallelStream().forEach(map -> {

			Iterator<Entry<String, Object>> ier = map.entrySet().iterator();
			while (ier.hasNext()) {
				Entry<String, Object> next = ier.next();
				if (next.getValue() instanceof Date) {
					DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String value = df1.format(next.getValue());
					next.setValue(value);
				}

			}

		});
		return list;
	}

	public static List<Map<String, Object>> query2Map(Query emQuery) {
		if (null == emQuery) {
			// 抛出异常
			throw new RuntimeException("没有任何查询语句，请检查sql");
		}
		emQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = emQuery.getResultList();
		list.parallelStream().forEach(map -> {

			Iterator<Entry<String, Object>> ier = map.entrySet().iterator();
			while (ier.hasNext()) {
				Entry<String, Object> next = ier.next();
				if (next.getValue() instanceof Date) {
					DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String value = df1.format(next.getValue());
					next.setValue(value);
				}

			}

		});
		return list;
	}

	/**
	 * 查询仅限于单个字段，只返回list
	 * 
	 * @param sql
	 * @param em
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> query2List(String sql, EntityManager em) {
		Query query = em.createNativeQuery(sql);

		List<T> list = query.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> query2List(Query emQuery) {
		if (null == emQuery) {
			// 抛出异常
			throw new RuntimeException("没有任何查询语句，请检查sql");
		}
		List<T> list = emQuery.getResultList();
		return list;
	}

	/**
	 * 获取sql查询数量
	 * 
	 * @param sql
	 * @param em
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static BigInteger countNum(String sql, EntityManager em) {
		BigInteger num = BigInteger.ZERO;
		List<BigInteger> list = em.createNativeQuery(sql).getResultList();
		if (!CollectionUtils.isEmpty(list)) {
			num = list.get(0);
		}
		return num;
	}

	public static BigInteger countNum(Query emQuery) {
		if (null == emQuery) {
			// 抛出异常
			throw new RuntimeException("没有任何查询语句，请检查sql");
		}
		BigInteger num = BigInteger.ZERO;

		List<BigInteger> list = emQuery.getResultList();
		if (!CollectionUtils.isEmpty(list)) {
			num = list.get(0);
		}
		return num;
	}

	/**
	 * 判断一个对象所有属性是否都无值 必须都是包装类
	 * 
	 * @param obj
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static boolean isAllEmpty(Object obj) throws IllegalArgumentException, IllegalAccessException {
		if (null == obj) {
			return true;
		}
		for (Field f : obj.getClass().getDeclaredFields()) {
			f.setAccessible(true);

			if (f.get(obj) != null && StringUtils.isNotBlank(f.get(obj).toString())) { // 判断字段是否为空，并且对象属性中的基本都会转为对象类型来判断

				return false;
			}
		}
		return true;
	}

	/**
	 * 把List<Object[]>结果转换成List<T>
	 * 
	 * @param list
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@Deprecated
	private static <T> List<T> castBean(List<Object[]> list, Class<T> clazz) throws Exception {
		List<T> returnList = new ArrayList<>();
		if (!list.isEmpty()) {

			Object[] co = list.get(0);
			Class[] c2 = new Class[co.length];
			for (int i = 0; i < co.length; i++) {
				c2[i] = co[i].getClass();

			}
			for (int i = 0; i < list.size(); i++) {
				Constructor<T> constructor = clazz.getConstructor(c2);
				returnList.add(constructor.newInstance(list.get(i)));
			}
		}
		return returnList;
	}

	/**
	 * null对象转换为空
	 * 
	 * @param str
	 */
	public static String nullToStr(Object str) {
		if (null == str) {
			return "";
		}
		if ("null".equals(str)) {
			return "";
		}
		return str.toString();
	}
}
