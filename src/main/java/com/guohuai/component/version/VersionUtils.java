package com.guohuai.component.version;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.guohuai.component.exception.AMPException;

public class VersionUtils {
	private static Logger logger = LoggerFactory.getLogger(VersionUtils.class);
	public static final Integer V160 = 10600;
	public static final Integer V170 = 10700;
	
	public static int getCompareVersion(String version) {
		if(StringUtils.isBlank(version) || !version.matches("^\\d+\\.\\d+\\.\\d+$")) {
			throw new AMPException("版本号格式错误");
		}else {
			String[] versionArray = version.split("\\.");
			return Integer.valueOf(versionArray[0]) * 10000 + Integer.valueOf(versionArray[1]) * 100 + Integer.valueOf(versionArray[2]);
		}
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: checkVersionV160
	 * @Description: 返回值含义:
	 * true,表示app端小于1.6版本 
	 * false,表示pc端或app端大于等于1.6版本
	 * @return boolean
	 * @date 2017年9月22日 上午10:39:26
	 * @since  1.0.0
	 */
	public static boolean checkVersionV160() {
		String version = getVersion();
		logger.info("processing-version:{}", version);
		boolean flag = false;
		if (!StringUtils.isBlank(version) && VersionUtils.getCompareVersion(version) < VersionUtils.V160) {
			flag = true;
		}
		logger.info("end--checkVersionV160(), version:{}, flag:{}", version, flag);
		return flag;
	}
	
	public static String getVersion() {
		logger.info("getVersion()获取版本号开始!");
		if (RequestContextHolder.getRequestAttributes() == null) {
			return "";
		}
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String version = request.getHeader("Version");
		logger.info("getVersion()获取版本号结束,version:{}", version);
		if (StringUtils.isBlank(version)) {
			version = "";
		}
		return version;
	}
	
	public static boolean checkVersionV170() {
		logger.info("begin--checkVersionV170()");
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String version = request.getHeader("Version");
		logger.info("processing-version:{}", version);
		boolean flag = false;
		if (!StringUtils.isBlank(version) && VersionUtils.getCompareVersion(version) < VersionUtils.V170) {
			flag = true;
		}
		logger.info("end--checkVersionV170(), version:{}, flag:{}", version, flag);
		return flag;
	}
}
