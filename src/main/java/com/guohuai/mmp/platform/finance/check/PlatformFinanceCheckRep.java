package com.guohuai.mmp.platform.finance.check;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-财务-三方对账
 * 
 * @author suzhicheng
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlatformFinanceCheckRep implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4886963831917236644L;
	String oid;
	/**
	 *对账批次号 
	 */
	private String checkCode;
	
	/**
	 * 对账日期
	 */
	private Date checkDate;
	
	/**
	 * 对账状态
	 */
	private String checkStatus;
	/**
	 * 确认状态
	 */
	private String confirmStatus;
	/**
	 * 总笔数
	 */
	private Integer totalCount;
	
	/**
	 * 错账笔数
	 */
	private Integer wrongCount;
	
	/**
	 * 同步数据导入状态
	 */
	private String checkDataSyncStatus;
	/**
	 * 经办人
	 */
	private String operator;
	
	private Timestamp createTime;

	private Timestamp updateTime;

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
	
	
}
