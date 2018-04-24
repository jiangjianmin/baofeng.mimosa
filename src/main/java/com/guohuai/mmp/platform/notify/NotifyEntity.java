package com.guohuai.mmp.platform.notify;

import java.io.Serializable;
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
@Table(name = "T_MONEY_PLATFORM_NOTIFY")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class NotifyEntity extends UUID implements Serializable {
	
	private static final long serialVersionUID = 2425452922159934702L;
	
	
	/** 通知类型--已拒绝 */
	public static final String NOTIFY_notifyType_refused = "refused";
	/** 通知类型--成功受理 */
	public static final String NOTIFY_notifyType_accepted = "accepted";
	/** 通知类型--结算完成 */
	public static final String NOTIFY_notifyType_done = "done";
	/** 通知类型--投资份额确认 */
	public static final String NOTIFY_notifyType_confirmed = "confirmed";
	/** 通知类型--订单完全赎回 */
	public static final String NOTIFY_notifyType_closed = "closed";
	/** 通知类型--已作废 */
	public static final String NOTIFY_notifyType_abandoned = "abandoned";
	/** 通知类型--订单部分赎回*/
	public static final String NOTIFY_notifyType_partHolding = "partHolding";
//	/** 通知类型--剩余可投变化*/
	public static final String NOTIFY_notifyType_investableVolumeChange = "investableVolumeChange";
	
	/** 通知状态--待确认 */
	public static final String ORDERLOG_notifyStatus_toConfirm = "toConfirm";
	/** 通知状态--已确认 */
	public static final String ORDERLOG_notifyStatus_confirmed = "confirmed";
	
	
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
	private int errorCode=SysConstant.INTEGER_defaultValue;
	
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
	private long seqId;
	
	private Timestamp createTime;
	private Timestamp updateTime;
	
}
