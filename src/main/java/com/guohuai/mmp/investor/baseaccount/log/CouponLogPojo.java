package com.guohuai.mmp.investor.baseaccount.log;

import java.io.Serializable;
import java.sql.Timestamp;

public class CouponLogPojo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7888399385377126207L;
	private String oid;
	
	/** 状态 SUCCESS:FAILED */
	private String status;
	
	/** 类型，REGISTER:注册,REFEREE:推荐人 */
	private String type;

	/** 已发送次数 */
	private Integer sendedTimes;
	
	/** 最多发送次数 */
	private Integer limitSendTimes;
	
	/** 下次调用时间 */
	private Timestamp nextNotifyTime;

	/** 用户ID */
	private String userOid;

	private Timestamp createTime;

	private Timestamp updateTime;

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the sendedTimes
	 */
	public Integer getSendedTimes() {
		return sendedTimes;
	}

	/**
	 * @param sendedTimes the sendedTimes to set
	 */
	public void setSendedTimes(Integer sendedTimes) {
		this.sendedTimes = sendedTimes;
	}

	/**
	 * @return the limitSendTimes
	 */
	public Integer getLimitSendTimes() {
		return limitSendTimes;
	}

	/**
	 * @param limitSendTimes the limitSendTimes to set
	 */
	public void setLimitSendTimes(Integer limitSendTimes) {
		this.limitSendTimes = limitSendTimes;
	}

	/**
	 * @return the nextNotifyTime
	 */
	public Timestamp getNextNotifyTime() {
		return nextNotifyTime;
	}

	/**
	 * @param nextNotifyTime the nextNotifyTime to set
	 */
	public void setNextNotifyTime(Timestamp nextNotifyTime) {
		this.nextNotifyTime = nextNotifyTime;
	}

	/**
	 * @return the userOid
	 */
	public String getUserOid() {
		return userOid;
	}

	/**
	 * @param userOid the userOid to set
	 */
	public void setUserOid(String userOid) {
		this.userOid = userOid;
	}

	/**
	 * @return the createTime
	 */
	public Timestamp getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime the createTime to set
	 */
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the updateTime
	 */
	public Timestamp getUpdateTime() {
		return updateTime;
	}

	/**
	 * @param updateTime the updateTime to set
	 */
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * @return the oid
	 */
	public String getOid() {
		return oid;
	}

	/**
	 * @param oid the oid to set
	 */
	public void setOid(String oid) {
		this.oid = oid;
	}
	
}
