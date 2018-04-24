package com.guohuai.cache.service;

import java.math.BigDecimal;

import com.guohuai.component.exception.AMPException;

public class CacheUtil {
	
	
	
	public static void assertGreatEqualZero(BigDecimal val) {
		if (null == val || val.compareTo(BigDecimal.ZERO) < 0) {
			throw new AMPException("参数小于零");
		}
	}
}
