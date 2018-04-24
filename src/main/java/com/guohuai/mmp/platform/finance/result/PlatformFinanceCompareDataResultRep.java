package com.guohuai.mmp.platform.finance.result;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlatformFinanceCompareDataResultRep implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -1956892210090284690L;
	/** oid */
	private String oid;
	/** 对账Oid */
	private String checkOid;
	/** 订单号 */
	private String orderCode;
	/** 订单类型 */
	private String orderType;
	/** 交易金额 */
	private BigDecimal orderAmount=BigDecimal.ZERO;
	/** 订单状态 */
	private String orderStatus;
	/** 业务日期 */
	private Date buzzDate;
	/** 投资人 */
	private String investorOid;
	/** 投资人姓名 */
	private String investorName;
	/** 对账订单号 */
	private String checkOrderCode;
	/** 对账订单类型 */
	private String checkOrderType;
	/** 对账交易金额 */
	private BigDecimal checkOrderAmount=BigDecimal.ZERO;
	/** 对账订单状态 */
	private String checkOrderStatus;
	/** 对账投资人 */
	private String checkInvestorOid;
	/** 对账投资人姓名 */
	private String checkInvestorName;
	/** 对账状态 */
	private String checkStatus;
	/** 处理结果 */
	private String dealStatus;
	/** 修改时间 */
	private Timestamp updateTime;
	/** 创建时间 */
	private Timestamp createTime;
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
	 * 获取 checkOid
	 */
	public String getCheckOid() {
		return checkOid;
	}
	/**
	 * 设置 checkOid
	 */
	public void setCheckOid(String checkOid) {
		this.checkOid = checkOid;
	}
	/**
	 * 获取 orderCode
	 */
	public String getOrderCode() {
		return orderCode;
	}
	/**
	 * 设置 orderCode
	 */
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	/**
	 * 获取 orderType
	 */
	public String getOrderType() {
		return orderType;
	}
	/**
	 * 设置 orderType
	 */
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	/**
	 * 获取 orderAmount
	 */
	public BigDecimal getOrderAmount() {
		return orderAmount;
	}
	/**
	 * 设置 orderAmount
	 */
	public void setOrderAmount(BigDecimal orderAmount) {
		this.orderAmount = orderAmount;
	}
	/**
	 * 获取 orderStatus
	 */
	public String getOrderStatus() {
		return orderStatus;
	}
	/**
	 * 设置 orderStatus
	 */
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
	/**
	 * 获取 buzzDate
	 */
	public Date getBuzzDate() {
		return buzzDate;
	}
	/**
	 * 设置 buzzDate
	 */
	public void setBuzzDate(Date buzzDate) {
		this.buzzDate = buzzDate;
	}
	/**
	 * 获取 investorOid
	 */
	public String getInvestorOid() {
		return investorOid;
	}
	/**
	 * 设置 investorOid
	 */
	public void setInvestorOid(String investorOid) {
		this.investorOid = investorOid;
	}
	/**
	 * 获取 checkOrderCode
	 */
	public String getCheckOrderCode() {
		return checkOrderCode;
	}
	/**
	 * 设置 checkOrderCode
	 */
	public void setCheckOrderCode(String checkOrderCode) {
		this.checkOrderCode = checkOrderCode;
	}
	/**
	 * 获取 checkOrderType
	 */
	public String getCheckOrderType() {
		return checkOrderType;
	}
	/**
	 * 设置 checkOrderType
	 */
	public void setCheckOrderType(String checkOrderType) {
		this.checkOrderType = checkOrderType;
	}
	/**
	 * 获取 checkOrderAmount
	 */
	public BigDecimal getCheckOrderAmount() {
		return checkOrderAmount;
	}
	/**
	 * 设置 checkOrderAmount
	 */
	public void setCheckOrderAmount(BigDecimal checkOrderAmount) {
		this.checkOrderAmount = checkOrderAmount;
	}
	/**
	 * 获取 checkOrderStatus
	 */
	public String getCheckOrderStatus() {
		return checkOrderStatus;
	}
	/**
	 * 设置 checkOrderStatus
	 */
	public void setCheckOrderStatus(String checkOrderStatus) {
		this.checkOrderStatus = checkOrderStatus;
	}
	/**
	 * 获取 checkInvestorOid
	 */
	public String getCheckInvestorOid() {
		return checkInvestorOid;
	}
	/**
	 * 设置 checkInvestorOid
	 */
	public void setCheckInvestorOid(String checkInvestorOid) {
		this.checkInvestorOid = checkInvestorOid;
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
	 * 获取 dealStatus
	 */
	public String getDealStatus() {
		return dealStatus;
	}
	/**
	 * 设置 dealStatus
	 */
	public void setDealStatus(String dealStatus) {
		this.dealStatus = dealStatus;
	}
	/**
	 * 获取 investorName
	 */
	public String getInvestorName() {
		return investorName;
	}
	/**
	 * 设置  investorName
	 */
	public void setInvestorName(String investorName) {
		this.investorName = investorName;
	}
	/**
	 * 获取 checkInvestorName
	 */
	public String getCheckInvestorName() {
		return checkInvestorName;
	}
	/**
	 * 设置  checkInvestorName
	 */
	public void setCheckInvestorName(String checkInvestorName) {
		this.checkInvestorName = checkInvestorName;
	}
	
	
}
