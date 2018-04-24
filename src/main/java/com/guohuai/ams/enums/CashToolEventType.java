/*   
 * Copyright © 2015 guohuaigroup All Rights Reserved.   
 *   
 * This software is the confidential and proprietary information of   
 * Founder. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Founder.   
 *   
 */
package com.guohuai.ams.enums;

/**
 * 现金管理类工具操作类型
 * <p>Title: CashToolEventType.java</p>    
 * <p>Description: 描述 </p>   
 * @author vania      
 * @version 1.0    
 * @created 2016年5月19日 下午6:46:55
 */
public enum CashToolEventType {
	
	/**
	 * 创建
	 */
	create("1", "创建"), 
	/**
	 * 创建
	 */
	edit("2", "编辑"), 
	/**
	 * 提交预审
	 */
	check("3", "提交预审"),
	/**
	 * 预审通过
	 */
	checkpass("4", "预审通过"),
	/**
	 * 驳回
	 */
	checkreject("5", "驳回"),
	/**
	 * 作废
	 */
	invalid("6", "作废"),
	/**
	 * 过会
	 */
	metting("7", "过会"),
	/**
	 * 预审
	 */
	collecting("8", "募集"),
	
	/**
	 * 成立
	 */
	establish("9", "成立"),
	/**
	 * 不成立
	 */
	unEstablish("10", "不成立"),
	
	/**
	 * 收益采集
	 */
	revenue("11", "收益采集"),
	/**
	 * 逾期
	 */
	overdue("12", "逾期"),
	/**
	 * 完成
	 */
	finish("13", "完成"),
	/**
	 * 移除
	 */
//	delete("14", "移除"),
	;
	private String code;
	private String desc;

	private CashToolEventType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
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
	public static CashToolEventType getType(String code) {
		for (CashToolEventType type : CashToolEventType.values()) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}
}
