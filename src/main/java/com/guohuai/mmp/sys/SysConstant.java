package com.guohuai.mmp.sys;

import java.math.BigDecimal;

public class SysConstant {

	/** 管理员-操作人-初始化线程 */
	public static final String ADMIN_OPERATOR_InitThread = "InitThread";
	/** 管理员-操作人-下午3点定时任务 */
	public static final String ADMIN_OPERATOR_PMTimer = "PMTimer";
	/** 管理员-操作人-凌晨0点定时任务 */
	public static final String ADMIN_OPERATOR_AMTimer = "AMTimer";
	
	/** 管理员-操作人-每隔五分钟放款定时任务 */
	public static final String ADMIN_OPERATOR_LEND_Timer = "LENDTimer";
	
	/** 管理员-操作人-每隔十分钟更新产品状态定时任务 */
	public static final String ADMIN_OPERATOR_PRODUCT_STATUS_Timer = "ProductStatusTimer";
	
	/** 管理员-操作人-支付回调 */
	public static final String ADMIN_OPERATOR_PaymentCallback = "PaymentCallback";
	
	/** 操作人-用户-自己 */
	public static final String OPERATOR_User_Self = "FrontEndUser";
	
	/**调用本系统的相关平台**/
	public static final String[] SYS_PLATORMS  = {"stock","money","wxmoney"};
	
	/** 实体类里面成员变量 BigDeciaml 默认值为0 **/
	public static final BigDecimal BIGDECIMAL_defaultValue = new BigDecimal(0);
	
	/** 产品由<<运行期>>自动转为<<到期完成>> BigDeciaml 判定值为0.01 **/
	public static final BigDecimal PRODUCT_BIGDECIMAL_running2end = new BigDecimal(0.01);
	
	/** 万份收益 * 3.56 % = 年化利率         */
	public static final BigDecimal TenThousandPerDayInterest2AnnualInterest = new BigDecimal(3.65);
	
	/** 万份收益 = 年化利率 / 3.65%  **/
	public static final BigDecimal AnnualInterest2TenThousandPerDayInterest = new BigDecimal(3.65);
	
	/** Integer 默认值为0 **/
	public static final Integer INTEGER_defaultValue = 0;
	
	/** 创建收益 - 自动 **/
	public static final String OPERATOR_INTEREST_AUTO = "自动";
	
	/** 平台配置 - 自动创建 **/
	public static final String OPERATOR_SYSCONFIG_AUTO = "autoCreate";
	

	
	/** Long 默认值为0 **/
	public static final Long LONG_defaultValue = 0l;
	
	/**
	 * 合仓数据
	 */
	public static final String LECURRENT_REDIS_HOLD_PRIFIX="lecurrent:H:";
	
	/**
	 * 阶梯持仓
	 */
	public static final String LECURRENT_REDIS_HOLDLEVEL_PRIFIX="lecurrent:HL:";
	
	/**
	 * 持仓明细分页用的zset
	 */
	public static final String LECURRENT_REDIS_HOLDDETIAILS_ZSET_PRIFIX="lecurrent:OI:";
	
	/**
	 * 持仓明细HASH
	 */
	public static final String LECURRENT_REDIS_HOLDDETIAILS_HASH_PRIFIX="lecurrent:O:";
	
	/**
	 * 协议文件
	 */
	public static final String LECURRENT_REDIS_AGREEMENT_HASH_PRIFIX="lecurrent:A:";
	
	
	
}
