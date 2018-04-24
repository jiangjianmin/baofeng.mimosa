package com.guohuai.component.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtil {
	
	
	public static String getLocalIP() {
//		return "127.127.0.1";
		InetAddress addr;
		String ip = null;
		try {
			addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		
		return ip;
	}
	
	public static void main(String[] args) {
		System.out.println(getLocalIP());
	}
}
