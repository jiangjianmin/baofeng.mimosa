package com.guohuai.component.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.mmp.platform.redis.RedisSyncService;

@Service
public class DecimalUtil {

	@Value("${bigdecimal.scale:2}")
	String intScale;
	public static int scale;

	@Value("${round.mode:1}")
	String intRoundMode;
	public static RoundingMode roundMode;
	

	public static final BigDecimal zoom = new BigDecimal(10000);

	static Logger logger = LoggerFactory.getLogger(DecimalUtil.class);

	@PostConstruct
	public void init() {
		scale = Integer.parseInt(intScale);
		roundMode = RoundingMode.valueOf(Integer.parseInt(intRoundMode));

	}

	public static BigDecimal setScaleDown(BigDecimal in) {
		if (null == in) {
			return in;
		}
		return in.setScale(scale, roundMode);
	}
	
	/**
	 * 是否走相应规则，等于0或为null表示 不走相关约束
	 */
	public static boolean isGoRules(BigDecimal in) {
		if (null != in && in.compareTo(BigDecimal.ZERO) != 0) {
			return true;
		}
		return false;
	}
	
	public static boolean isGoRules(Integer in) {
		if (null != in && in != 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * valOut是否大于等于0,否则抛出对应的异常
	 * @param valOut
	 * @param errorCode
	 */
	public static void isValOutGreatThanOrEqualZero(BigDecimal valOut, int errorCode) {
		if (null == valOut || valOut.compareTo(BigDecimal.ZERO) < 0) {
			
			throw AMPException.getException(errorCode);
		}
	}
	
	
	
	public static BigDecimal zoomOut(BigDecimal amount) {
		return zoomOut(amount, DecimalUtil.zoom);
	}
	
	/**
	 * 放大zoom倍
	 */
	public static BigDecimal zoomOut(BigDecimal amount, int zoom) {
		return zoomOut(amount, new BigDecimal(zoom), scale);
	}
	
	public static BigDecimal zoomOut(BigDecimal amount, BigDecimal zoom) {
		return zoomOut(amount, zoom, scale);
	}
	
	public static BigDecimal zoomOut(BigDecimal amount, int zoom, int scale) {
		return zoomOut(amount, new BigDecimal(zoom), scale);
	}
	
	public static void main(String[] args) {
		System.out.println(zoomOut(new BigDecimal(0.07),100,2));
	}
	
	public static BigDecimal zoomOut(BigDecimal amount, BigDecimal zoom, int scale) {
		if (null == amount) {
			return amount;
		}
		return amount.multiply(zoom).setScale(scale,RoundingMode.DOWN);
	}

	
	public static BigDecimal zoomIn(BigDecimal amount) {
		return zoomIn(amount, DecimalUtil.zoom);
	}
	
	/**
	 * 缩小zoom倍
	 */
	public static BigDecimal zoomIn(BigDecimal amount, int zoom) {
		return zoomIn(amount, new BigDecimal(zoom));
	}

	public static BigDecimal zoomIn(BigDecimal amount, BigDecimal zoom) {
		if (null == amount) {
			return BigDecimal.ZERO;
		}
		return amount.divide(zoom);
	}
	
	public static BigDecimal null2Zero(BigDecimal in) {
		if (null == in) {
			return BigDecimal.ZERO;
		}
		return in;
	}

	/**
	 * 缩小10000
	 */
	public static void zoomIn(Object obj) {
		try {
			String[] arrV = (String[]) obj.getClass().getField("zoomArr").get(obj);
			for (String methodName : arrV) {

				methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
				BigDecimal val = (BigDecimal) obj.getClass().getMethod("get" + methodName).invoke(obj);
				val = DecimalUtil.zoomIn(val);
				
				obj.getClass().getMethod("set" + methodName, BigDecimal.class).invoke(obj, val);

			}
		} catch (Exception e) {
			logger.error("failed", e);
			e.printStackTrace();
		}
	}

	public static void zoomOut(Object obj) {

		try {
			String[] arrV = (String[])obj.getClass().getField("zoomArr").get(obj);
			for (String methodName : arrV) {
				methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
				BigDecimal val = (BigDecimal) obj.getClass().getMethod("get" + methodName).invoke(obj);
				val = DecimalUtil.zoomOut(val);

				obj.getClass().getMethod("set" + methodName, BigDecimal.class).invoke(obj, val);
			}
		} catch (Exception e) {
			logger.error("failed", e);
			e.printStackTrace();
		}

	}

}
