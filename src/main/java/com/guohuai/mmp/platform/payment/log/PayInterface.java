package com.guohuai.mmp.platform.payment.log;

public enum PayInterface {
	// 投资支付
	pay("pay", -1, "com.guohuai.mmp.platform.payment.PayRequest"), 
	// 赎回
	payee("payee", 10, "com.guohuai.mmp.platform.payment.RedeemPayRequest"),
	//赎回回调
	tradeCallback("tradeCallback", -1),
	//交易查询
	queryPay("queryPay", -1);
	String interfaceName;
	/**
	 * 最多发送次数
	 * -1:表示不重发
	 */
	int limitSendTimes;
	String ireq;
	
	private PayInterface(String interfaceName, int limitSendTimes) {
		this.interfaceName = interfaceName;
		this.limitSendTimes = limitSendTimes;
	}
	
	private PayInterface(String interfaceName, int limitSendTimes, String ireq) {
		this.interfaceName = interfaceName;
		this.limitSendTimes = limitSendTimes;
		this.ireq = ireq;
	}
	
	public String getInterfaceName() {
		return interfaceName;
	}
	
	public static int getTimes(String interfaceName) {
		for (PayInterface tmp : PayInterface.values()) {
			if (tmp.getInterfaceName().equals(interfaceName)) {
				return tmp.limitSendTimes;
			}
		}
		throw new IllegalArgumentException("interfaceName does not exist ");
	}
	
	public static String getIReq(String interfaceName) {
		for (PayInterface tmp : PayInterface.values()) {
			if (tmp.getInterfaceName().equals(interfaceName)) {
				return tmp.ireq;
			}
		}
		throw new IllegalArgumentException("interfaceName does not exist ");
	}
	
	
}
