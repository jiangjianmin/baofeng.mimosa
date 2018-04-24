package com.guohuai.mmp.platform.payment;

@lombok.Data
public class OrderNotifyReq {
	private String returnCode;
	private String orderCode;
	private  String errorMessage;
	private String payChannel;
}
