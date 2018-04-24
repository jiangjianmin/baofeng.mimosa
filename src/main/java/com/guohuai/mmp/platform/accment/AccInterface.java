package com.guohuai.mmp.platform.accment;

public enum AccInterface {
	//支付
	trade("trade", 10, "com.guohuai.mmp.platform.accment.TradeRequest"), 
	//转账
	transferAccount("transferAccount", 10, "com.guohuai.mmp.platform.accment.TransferAccRequest"),
	//创建用户下账户
	createAccount("createAccount", -1, "com.guohuai.mmp.platform.accment.CreateAccRequest"),
	//调账
	enterAccout("enterAccout", -1, "com.guohuai.mmp.platform.accment.EnterAccRequest"),
	//创建用户
	addUser("addUser", -1), 
	//查询用户下账户
	accountQueryList("accountQueryList", -1),
	tradepublish("tradepublish", 10, "com.guohuai.mmp.platform.accment.TpIntegratedRequest"),
	//查询用户
	userQueryList("userQueryList", -1);
	String interfaceName;
	/**
	 * 最多发送次数
	 * -1:表示不重发
	 */
	int limitSendTimes;
	String ireq;
	
	private AccInterface(String interfaceName, int limitSendTimes) {
		this.interfaceName = interfaceName;
		this.limitSendTimes = limitSendTimes;
	}
	
	private AccInterface(String interfaceName, int limitSendTimes, String ireq) {
		this.interfaceName = interfaceName;
		this.limitSendTimes = limitSendTimes;
		this.ireq = ireq;
	}
	
	public String getInterfaceName() {
		return interfaceName;
	}
	
	public static int getTimes(String interfaceName) {
		for (AccInterface tmp : AccInterface.values()) {
			if (tmp.getInterfaceName().equals(interfaceName)) {
				return tmp.limitSendTimes;
			}
		}
		throw new IllegalArgumentException("interfaceName does not exist ");
	}
	
	public static String getIReq(String interfaceName) {
		for (AccInterface tmp : AccInterface.values()) {
			if (tmp.getInterfaceName().equals(interfaceName)) {
				return tmp.ireq;
			}
		}
		throw new IllegalArgumentException("interfaceName does not exist ");
	}
	
	
}
