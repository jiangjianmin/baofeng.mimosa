package com.guohuai.mmp.serialtask;

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
 * 串行任务执行
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_SERIALTASK")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class SerialTaskEntity extends UUID {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6214778979372528113L;

	/** 任务代码--申购 */
	public static final String TASK_taskCode_invest = "invest";
	/** 任务代码--赎回 */
	public static final String TASK_taskCode_redeem = "redeem";
	/** 任务代码--赎回 */
	public static final String TASK_taskCode_specialRedeem = "specialRedeem";
	/** 任务代码--活转定 */
	public static final String TASK_taskCode_redeemInvest = "redeemInvest";
	/** 任务代码--发行人清算 */
	public static final String TASK_taskCode_publisherClear = "publisherClear";
	/** 任务代码--发行人份额确认 */
	public static final String TASK_taskCode_publisherConfirm = "publisherConfirm";
	/** 任务代码--发行人结算 */
	public static final String TASK_taskCode_publisherClose = "publisherClose";
	/** 任务代码--二级邀请定期奖励收益明细生成 */
	public static final String TASK_taskCode_tnProfitInit = "tnProfitInit";

	/** 任务代码--计息 */
	public static final String TASK_taskCode_interest = "interest";
	/** 任务代码--解锁可计息代码 */
	public static final String TASK_taskCode_unlockAccrual = "unlockAccrual";
	/** 任务代码--解锁可赎回代码 */
	public static final String TASK_taskCode_unlockRedeem = "unlockRedeem";
	/** 任务代码--重置今日统计 */
	public static final String TASK_taskCode_resetToday = "resetToday";
	/** 任务代码--废单 */
	public static final String TASK_taskCode_abandon = "abandon";
	/** 任务代码--退款 */
	public static final String TASK_taskCode_refund = "refund";
	
	/** 任务状态--待执行 */
	public static final String TASK_taskStatus_toRun = "toRun";
	/** 任务状态--执行中 */
	public static final String TASK_taskStatus_running = "running";
	/** 任务状态--超时 */
	public static final String TASK_taskStatus_timed = "timed";
	/** 任务状态--失败 */
	public static final String TASK_taskStatus_failed = "failed";
	/** 任务状态--完成 */
	public static final String TASK_taskStatus_done = "done";
	
	
	/**
	 * 任务代码
	 */
	private String taskCode;
	
	/**
	 * 任务参数
	 */
	private String taskParams;
	
	/**
	 * 执行开始时间
	 */
	private Timestamp executeStartTime;
	
	/**
	 * 执行结束时间
	 */
	private Timestamp executeEndTime;
	
	/**
	 * 任务状态
	 */
	private String taskStatus;
	
	/**
	 * 优先级
	 */
	private Integer priority=SysConstant.INTEGER_defaultValue;
	
	/**
	 * 任务异常
	 */
	private String taskError;
	
	private Timestamp updateTime;

	private Timestamp createTime;

}
