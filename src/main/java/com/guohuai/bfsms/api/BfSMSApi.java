package com.guohuai.bfsms.api;

import feign.Param;
import feign.RequestLine;

public interface BfSMSApi {

//	@RequestLine("POST /QxtSms/QxtFirewall")
//	public String sendSMS(@Param("OperID") String OperID, 
//			@Param("OperPass") String OperPass,
//			@Param("AppendID") String AppendID,
//			@Param("DesMobile") String DesMobile,
//			@Param("Content") String Content,
//			@Param("ContentType") String ContentType);
	@RequestLine("POST /axj_http_server/sms")
	public String sendSMS(@Param("name") String OperID, 
			@Param("pass") String OperPass,
			@Param("mobiles") String DesMobile,
			@Param("content") String Content);
}
