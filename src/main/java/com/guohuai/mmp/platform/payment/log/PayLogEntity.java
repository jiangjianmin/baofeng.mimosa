package com.guohuai.mmp.platform.payment.log;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 支会日志
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PAY_LOG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PayLogEntity extends UUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1125580834556322202L;
	
	public static final String PAY_handleType_applyCall = "applyCall";
	public static final String PAY_handleType_notify = "notify";
	public static final String PAY_handleType_query = "query";

	/** 接口名称 */
	private String interfaceName;
	
	/** 订单号 */
	private String orderCode;
	
	/** 调用类型 */
	private String handleType;

	/** 接口返回码 */
	private Integer errorCode;

	/** 接口错误消息 */
	private String errorMessage;

	/** 已发送次数 */
	private Integer sendedTimes;
	
	/**
	 * 最多发送次数
	 */
	private Integer limitSendTimes;
	
	/**
	 * 下次调用时间
	 */
	private Timestamp nextNotifyTime;

	/** 发送消息内容 */
	private String content;

	private Timestamp createTime;

	private Timestamp updateTime;
}
