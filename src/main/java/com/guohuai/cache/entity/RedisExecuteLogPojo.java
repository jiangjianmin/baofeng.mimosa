package com.guohuai.cache.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class RedisExecuteLogPojo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5944117733266362891L;
	String oid;
	
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
	/**
	 * 获取 batchNo
	 */
	public String getBatchNo() {
		return batchNo;
	}
	/**
	 * 设置 batchNo 
	 */
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	/**
	 * 获取 executeCommand
	 */
	public String getExecuteCommand() {
		return executeCommand;
	}
	/**
	 * 设置 executeCommand 
	 */
	public void setExecuteCommand(String executeCommand) {
		this.executeCommand = executeCommand;
	}
	/**
	 * 获取 hkey
	 */
	public String getHkey() {
		return hkey;
	}
	/**
	 * 设置 hkey 
	 */
	public void setHkey(String hkey) {
		this.hkey = hkey;
	}
	/**
	 * 获取 field
	 */
	public String getField() {
		return field;
	}
	/**
	 * 设置 field 
	 */
	public void setField(String field) {
		this.field = field;
	}
	/**
	 * 获取 value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * 设置 value 
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * 获取 backValue
	 */
	public String getBackValue() {
		return backValue;
	}
	/**
	 * 设置 backValue 
	 */
	public void setBackValue(String backValue) {
		this.backValue = backValue;
	}
	/**
	 * 获取 errorCommand
	 */
	public String getErrorCommand() {
		return errorCommand;
	}
	/**
	 * 设置 errorCommand 
	 */
	public void setErrorCommand(String errorCommand) {
		this.errorCommand = errorCommand;
	}
	/**
	 * 获取 executeTime
	 */
	public Timestamp getExecuteTime() {
		return executeTime;
	}
	/**
	 * 设置 executeTime 
	 */
	public void setExecuteTime(Timestamp executeTime) {
		this.executeTime = executeTime;
	}
	/**
	 * 获取 executeSuccessStatus
	 */
	public String getExecuteSuccessStatus() {
		return executeSuccessStatus;
	}
	/**
	 * 设置 executeSuccessStatus 
	 */
	public void setExecuteSuccessStatus(String executeSuccessStatus) {
		this.executeSuccessStatus = executeSuccessStatus;
	}
	/**
	 * 获取 executeFailedStatus
	 */
	public String getExecuteFailedStatus() {
		return executeFailedStatus;
	}
	/**
	 * 设置 executeFailedStatus 
	 */
	public void setExecuteFailedStatus(String executeFailedStatus) {
		this.executeFailedStatus = executeFailedStatus;
	}
	/**
	 * 获取 errorCount
	 */
	public Integer getErrorCount() {
		return errorCount;
	}
	/**
	 * 设置 errorCount 
	 */
	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}
	/**
	 * 获取 createTime
	 */
	public Timestamp getCreateTime() {
		return createTime;
	}
	/**
	 * 设置 createTime 
	 */
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	/**
	 * 获取 oid
	 */
	public String getOid() {
		return oid;
	}
	/**
	 * 设置 oid 
	 */
	public void setOid(String oid) {
		this.oid = oid;
	}
	
	
}
