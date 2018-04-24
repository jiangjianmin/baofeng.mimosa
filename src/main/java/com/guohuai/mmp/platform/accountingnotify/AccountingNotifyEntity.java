package com.guohuai.mmp.platform.accountingnotify;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台_支付_调用日志
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_ACCOUNTINGNOTIFY")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class AccountingNotifyEntity extends UUID implements Serializable {
	
	private static final long serialVersionUID = 2425452922159934702L;
	
	
	/** 通知类型--轧差支付 */
	public static final String NOTIFY_notifyType_offsetPay = "offsetPay";
	/** 通知类型--轧差支付-手续费 */
	public static final String NOTIFY_notifyType_offsetPayFee = "offsetPayCouFee";
	/** 通知类型--轧差收款 */
	public static final String NOTIFY_notifyType_offsetCollect = "offsetCollect";
	/** 通知类型--申购订单 */
	public static final String NOTIFY_notifyType_invest = "invest";
	/** 通知类型--确认费用 */
	public static final String NOTIFY_notifyType_platformFee = "platformFee";
	/** 通知类型--支付平台费用 */
	public static final String NOTIFY_notifyType_payPlatform = "payPlatformFee";
	/** 通知类型--支付平台费用-手续费 */
	public static final String NOTIFY_notifyType_payPlatformFee = "payPlatformCouFee";
	/** 通知类型--赎回 */
	public static final String NOTIFY_notifyType_redeem= "redeem";
	/** 通知类型--投资者收益*/
	public static final String NOTIFY_notifyType_investorIncome= "investorIncome";
	
	/** 通知状态--待确认 */
	public static final String ORDERLOG_notifyStatus_toConfirm = "toConfirm";
	/** 通知状态--已确认 */
	public static final String ORDERLOG_notifyStatus_confirmed = "confirmed";
	
	/** 事件类型 --轧差支付 */
	public static final String NOTIFY_EVENTCODE_GZZF = "GLS_ZCZF";
	
	/** 事件类型 --轧差支收款*/
	public static final String NOTIFY_EVENTCODE_GZSK = "GLS_ZCSK";

	/** 事件类型 --确认理财产品*/
	public static final String NOTIFY_EVENTCODE_QRLCCP = "GLS_QRLCCP";
	
	/** 事件类型 --计提应付客户利息*/
	public static final String NOTIFY_EVENTCODE_JTKHLX = "GLS_JTKHLX";
	
	/** 事件类型 --确认费用*/
	public static final String NOTIFY_EVENTCODE_QRFY = "GLS_QRFY";
	
	/** 事件类型 --支付费用*/
	public static final String NOTIFY_EVENTCODE_ZFFY = "GLS_ZFFY";
	
	/** 事件类型 --兑付理财产品*/
	public static final String NOTIFY_EVENTCODE_DFLCCP = "GLS_DFLCCP";
	
	/** LEXIN平台ID */
	public static final String PLATFORM_CUSTOMER_ID = "PLATFORM_CUSTOMER_ID";
	
	/** LEXIN平台ACCOUNT */
	public static final String PLATFORM_CUSTOMER_ACCOUNT = "PLATFORM_CUSTOMER_ACCOUNT";
	
	/** 业务类型编码后缀配置 */
	public static final String BUSINESS_CODE_SUFFIX = "_CODE";

	/**
	 * 通知编号	
	 */
	private String notifyId;
	
	/**
	 * 通知类型	
	 */
	private String notifyType;
	/**
	 * 通知内容	
	 */
	private String notifyContent;

	
	/**
	 * 通知状态	
	 */
	private String notifyStatus;
	
	/**
	 * 错误代码
	 */
	private Integer errorCode=SysConstant.INTEGER_defaultValue;
	
	/**
	 * 错误消息
	 */
	private String errorMessage;
	
	/**
	 * 通知时间	
	 */
	private Timestamp notifyTime;
	/**
	 * 回复时间	
	 */
	private Timestamp notifyConfirmedTime;
	/**
	 * 通知次数	
	 */
	private int notifyTimes;
	
	/**
	 * 序列
	 */
	private String seqId;
	
	/**
	 * 业务日期
	 */
	private Date busDate;
	
	/**
	 * 产品oid
	 */
	private String productOid;
	
	/**
	 * 渠道oid
	 */
	private String channelOid;
	
	/**
	 * 费用
	 */
	private BigDecimal costFee;
	
	private Timestamp createTime;
	private Timestamp updateTime;
	
}
