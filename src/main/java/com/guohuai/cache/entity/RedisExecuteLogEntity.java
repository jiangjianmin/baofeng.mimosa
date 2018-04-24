package com.guohuai.cache.entity;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import com.guohuai.component.persist.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_MONEY_CACHE_EXECUTE_LOG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class RedisExecuteLogEntity extends UUID {

	private static final long serialVersionUID = 3932627297952686751L;
	
	/** 整数 加 命令 */
	public static final String HINCRBY = "HINCRBY";
	/** 浮点数 加 命令 */
	public static final String HINCRBYFLOAT = "HINCRBYFLOAT";
	/** 插入数据 */
	public static final String HMSET = "HMSET";
	/** 修改字段 */
	public static final String HSET = "HSET";
	public static final String ZADD = "ZADD";
	/** 成功 */
	public static final String EXECUTE_STATUS_SUCCESS = "SUCCESS";
	/** 失败 */
	public static final String EXECUTE_STATUS_FAILED = "FAILED";

	/** 批次号 */
	String batchNo;
	/** 执行命令 */
	String executeCommand;
	/** hkey */
	String hkey;
	/** 修改的字段 */
	String field;
	/** 修改的值 */
	String value;
	/** 回滚修改的值 */
	String backValue;
	/** 错误执行命令 */
	String errorCommand;
	/** 执行时间 */
	Timestamp executeTime;
	/** 成功执行状态 SUCCESS:FAILED */
	String executeSuccessStatus;
	/** 失败执行状态 SUCCESS:FAILED */
	String executeFailedStatus;
	/** 失败执行次数(最多执行三次) */
	Integer errorCount;
	/** 创建时间 */
	Timestamp createTime;
}
