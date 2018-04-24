package com.guohuai.component.util;

import javax.servlet.http.HttpServletRequest;
/**
 * 
 * @author Jeffrey.Wong
 * 2015年7月22日下午6:45:03
 */
public class RemoteUtil {
	public static final String FORWARD_HEADER = "X-FORWARDED-FOR";
	
	public static String getRemoteAddr(HttpServletRequest request) {
		// 优先使用代理转发的地址
		String ip="";
//		if (request != null) {
//			ip = request.getHeader(FORWARD_HEADER);
//			if (null == ip) {
//				ip = request.getRemoteAddr();
//			}	
//		}
//		
//		if (ip == null || "".equals(ip) || ip.startsWith("0") ||ip.startsWith("127.0.0.1")) {
//			ip="115.29.145.24";
//		}
		return ip;
	}
}
