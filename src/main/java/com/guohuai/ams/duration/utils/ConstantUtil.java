package com.guohuai.ams.duration.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ConstantUtil {

	/**
	 * 初始化数值
	 */
	public static final BigDecimal init0 	= new BigDecimal("0");
	public static final BigDecimal NUM1 	= new BigDecimal("1");
	public static final BigDecimal NUM1_1 	= new BigDecimal("1.1");
	public static final BigDecimal NUM100 	= new BigDecimal("100");
	public static final BigDecimal NUM10000 = new BigDecimal("10000");
	public static final BigDecimal NUM365 	= new BigDecimal("365");
	
	/**
	 * 申赎类型
	 */
	public static final String PURCHASE 	= "purchase";	// 申购
	public static final String REDEEM 		= "redeem";		// 赎回
	
	/**
	 * 资产池操作
	 */
	public static final String IN 					= "划入";
	public static final String INVESTMENT 			= "投资";
	public static final String OUT 					= "划出";
	public static final String EDIT_SELF_USASSET 	= "编辑自有非标资产";
	public static final String EDIT_USASSET 		= "编辑非标资产";
	public static final String DELETE 				= "删除";
	public static final String DELETE_USASSET 		= "删除非标资产";
	public static final String REFUNDMENT 			= "退款";
	public static final String EXPIRED 				= "已到期";
	public static final String ADD_BANK 			= "新增银行资产";
	public static final String EDIT_BANK 			= "调整银行资产";
	
	/**
	 * 申赎
	 */
	public static final String SG 					= "PURCHASE";
	public static final String SH 					= "REDEEM";
	
	/**
	 * 申赎的资产状态
	 */
	public static final int STATUS_DET 				= -2;	// 已失效
	public static final int STATUS_WDSGR 			= -1;	// 未到申购日
	public static final int STATUS_YDSGRWQX 		= 0;	// 已到申购日未起息
	public static final int STATUS_YQX 				= 1;	// 已起息
	public static final int STATUS_YTZ 				= 2;	// 已调整
	public static final int STATUS_SYJZ 			= 3;	// 收益截止
	public static final int STATUS_SH 				= 4;	// 赎回
	public static final int STATUS_QBSH 			= 5;	// 全部赎回
	
	/**
	 * 资产来源
	 */
	public static final String PT 					= "PLATFORM";
	public static final String ZY 					= "SELF";
	
	/**
	 * 付息操作
	 */
	public static final String FULL 	= "一次性还本付息";
	// 一次性
	public static final String OTP 		= "OTP";
	// 周期
	public static final String CYCLE 	= "CYCLE";
	
	public static final Map<String, String> PAY_MAP = new HashMap<String, String>();
	static {
		PAY_MAP.put("按周期付息", "CYCLE");
		PAY_MAP.put("按月付息", "M");
		PAY_MAP.put("按季付息", "Q");
		PAY_MAP.put("按半年付息", "H_Y");
		PAY_MAP.put("按年付息", "Y");
		PAY_MAP.put("到期一次性", "OTP");
		PAY_MAP.put("NATURAL_YEAR", "NATURAL");
		PAY_MAP.put("CONTRACT_YEAR", "CONTRACT");
	}
}
