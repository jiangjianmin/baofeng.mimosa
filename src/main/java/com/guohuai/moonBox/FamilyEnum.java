package com.guohuai.moonBox;

 

public enum FamilyEnum {
	isAuto0("0","自动扣款"),
	isAuto1("1","用户主动扣款"),
	operateStatus0("0","新增"),
	operateStatus1("1","修改"),
	operateStatus2("2","删除"),
	investStatus1("1","扣款成功"),
	investStatus2("2","扣款失败"),
	investStatus3("3","扣款中"),
	redisExists0("0","redis键存在"),
	redisExists1("1","redis键不存在"),
	return0000("0000","操作成功"),
	return0001("0001","用户编号不能为空"),
	return0002("0002","产品编号不能为空"),
	return0003("0003","产品信息不存在"),
	return0004("0004","存在待扣款记录"),
	return0005("0005","当日为系统自动扣款日"),
	return0006("0006","明天为系统自动扣款日"),
	return0007("0007","协议金额不能为空"),
	return0008("0008","协议每月转入日期不能为空"),
	return0009("0009","下次转入日期不能为空"),
	return0010("0010","协议编号不能为空"),
	return1000("1000","协议信息不存在"),
	return1001("1001","用户状态异常"),
	return1002("1002","用户未绑定银行卡"),
	return1003("1003","当月无需在执行扣款计划"),
	return1004("1004","暂无可投资的活期产品"),
	return9998("9998","该活动已结束"),
	return9999("9999","系统异常"),
	protocalName("moonBox","月光宝盒"),
	protocalStatus0("0","开通"),
	protocalStatus1("1","作废");
	private String code;
	private String name;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private FamilyEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}

	@Override
	public String toString() {
		return this.code;
	}

	/**
	 * 通过code取得类型
	 * 
	 * @param code
	 * @return
	 */
	public static String getName(String code) {
		for (FamilyEnum type : FamilyEnum.values()) {
			if (type.getCode().equals(code)) {
				return type.getName();
			}
		}
		return null;
	}
}
