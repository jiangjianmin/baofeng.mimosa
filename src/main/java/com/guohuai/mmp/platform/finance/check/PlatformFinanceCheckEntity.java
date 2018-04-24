package com.guohuai.mmp.platform.finance.check;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.guohuai.basic.component.ext.hibernate.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-财务-三方对账
 * 
 * @author suzhicheng
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_FINANCE_CHECK")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PlatformFinanceCheckEntity extends UUID implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1646108955968857279L;
	public static final String PREFIX="CHEKDATA-";

	/** 对账状态--待对账 */
	public static final String  CHECKSTATUS_TOCHECK = "toCheck";
	/** 对账状态--对账中 */
	public static final String  CHECKSTATUS_CHECKING = "checking";
	/** 对账状态--对账成功 */
	public static final String  CHECKSTATUS_CHECKSUCCESS = "checkSuccess";
	/** 对账状态--对账失败 */
	public static final String  CHECKSTATUS_CHECKFAILED = "checkFailed";
	
	/** 确认状态--已确认 */
	public static final String  CONFIRMSTATUS_YES = "yes";
	/** 确认状态--未确认 */
	public static final String  CONFIRMSTATUS_NO = "no";
	
	/** 对账数据同步状态--待同步 */
	public static final String  CHECKDATASYNCSTATUS_toSync = "toSync";
	/** 对账数据同步状态--同步失败 */
	public static final String  CHECKDATASYNCSTATUS_syncFailed = "syncFailed";
	/** 对账数据同步状态--同步成功 */
	public static final String  CHECKDATASYNCSTATUS_syncOK = "syncOK";
	/** 对账数据同步状态--导入中 */
	public static final String  CHECKDATASYNCSTATUS_syncing = "syncing";
	
	
	/** 对账批次号   */
	private String checkCode;
	/** 对账日期  */
	private Date checkDate;
	/** 对账状态 */
	private String checkStatus;
	/** 确认状态 */
	private String confirmStatus;
	/** 总笔数 */
	private Integer totalCount;
	/** 数据同步状态 */
	private String checkDataSyncStatus;
	/** 错账笔数 */
	private Integer wrongCount;
	/** 经办人 */
	private String operator;
	private Timestamp createTime;
	private Timestamp updateTime;
	/** 开始时间 */
	private java.util.Date beginTime;
	/** 结束时间 */
	private java.util.Date endTime;
	/**
	 * 获取 checkCode
	 */
	public String getCheckCode() {
		return checkCode;
	}
	/**
	 * 设置 checkCode
	 */
	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}
	/**
	 * 获取 checkDate
	 */
	public Date getCheckDate() {
		return checkDate;
	}
	/**
	 * 设置 checkDate
	 */
	public void setCheckDate(Date checkDate) {
		this.checkDate = checkDate;
	}
	/**
	 * 获取 checkStatus
	 */
	public String getCheckStatus() {
		return checkStatus;
	}
	/**
	 * 设置 checkStatus
	 */
	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}
	/**
	 * 获取 confirmStatus
	 */
	public String getConfirmStatus() {
		return confirmStatus;
	}
	/**
	 * 设置 confirmStatus
	 */
	public void setConfirmStatus(String confirmStatus) {
		this.confirmStatus = confirmStatus;
	}
	/**
	 * 获取 totalCount
	 */
	public Integer getTotalCount() {
		return totalCount;
	}
	/**
	 * 设置 totalCount
	 */
	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
	/**
	 * 获取 checkDataSyncStatus
	 */
	public String getCheckDataSyncStatus() {
		return checkDataSyncStatus;
	}
	/**
	 * 设置 checkDataSyncStatus
	 */
	public void setCheckDataSyncStatus(String checkDataSyncStatus) {
		this.checkDataSyncStatus = checkDataSyncStatus;
	}
	/**
	 * 获取 wrongCount
	 */
	public Integer getWrongCount() {
		return wrongCount;
	}
	/**
	 * 设置 wrongCount
	 */
	public void setWrongCount(Integer wrongCount) {
		this.wrongCount = wrongCount;
	}
	/**
	 * 获取 operator
	 */
	public String getOperator() {
		return operator;
	}
	/**
	 * 设置 operator
	 */
	public void setOperator(String operator) {
		this.operator = operator;
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
	 * 获取 updateTime
	 */
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	/**
	 * 设置 updateTime
	 */
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	/**
	 * 获取 beginTime
	 */
	public java.util.Date getBeginTime() {
		return beginTime;
	}
	/**
	 * 设置  beginTime
	 */
	public void setBeginTime(java.util.Date beginTime) {
		this.beginTime = beginTime;
	}
	/**
	 * 获取 endTime
	 */
	public java.util.Date getEndTime() {
		return endTime;
	}
	/**
	 * 设置  endTime
	 */
	public void setEndTime(java.util.Date endTime) {
		this.endTime = endTime;
	}
	
	
}
