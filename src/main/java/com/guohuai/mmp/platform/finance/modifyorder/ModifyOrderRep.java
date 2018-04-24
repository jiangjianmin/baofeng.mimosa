package com.guohuai.mmp.platform.finance.modifyorder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModifyOrderRep implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3520481608619284516L;
	/** oid */
	private String oid;
	/** 所属对账批次 */
	private String checkCode;
	/** 产品ID */
	private String productOid;
	/** 所属产品 */
	private String productName;
	/** 订单号 */
	private String orderCode;
	/** 所属订单 */
	private String tradeType;
	/** 订单金额  */
	private BigDecimal orderAmount;
	/** 操作类型  */
	private String opType;
	/** 修改前状态 */
	private String premodifyStatus;
	/** 修改后状态 */
	private String postmodifyStatus;
	/** 原因 */
	private String reason;
	/**  审核状态 */
	private String approveStatus;
	/** 操作人 */
	private String operator;
	/** 投资者Oid */
	private String investorOid;
	/** 处理结果 */
	private String dealStatus;
	/** 所属对账结果 */
	private String resultOid;
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
	 * 获取 checkOid
	 */
	public String getCheckCode() {
		return checkCode;
	}

	/**
	 * 设置 productName
	 */
	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}
	
	/**
	 * 获取 productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * 设置 productOid
	 */
	public void setProductName(String productName) {
		this.productName = productName;
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
	 * 获取 tradeType
	 */
	public String getTradeType() {
		return tradeType;
	}

	/**
	 * 设置 tradeType
	 */
	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
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
	 * 获取 opType
	 */
	public String getOpType() {
		return opType;
	}

	/**
	 * 设置 opType
	 */
	public void setOpType(String opType) {
		this.opType = opType;
	}

	/**
	 * 获取 premodifyStatus
	 */
	public String getPremodifyStatus() {
		return premodifyStatus;
	}

	/**
	 * 设置 premodifyStatus
	 */
	public void setPremodifyStatus(String premodifyStatus) {
		this.premodifyStatus = premodifyStatus;
	}

	/**
	 * 获取 postmodifyStatus
	 */
	public String getPostmodifyStatus() {
		return postmodifyStatus;
	}

	/**
	 * 设置 postmodifyStatus
	 */
	public void setPostmodifyStatus(String postmodifyStatus) {
		this.postmodifyStatus = postmodifyStatus;
	}

	/**
	 * 获取 reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * 设置 reason
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * 获取 approveStatus
	 */
	public String getApproveStatus() {
		return approveStatus;
	}

	/**
	 * 设置 approveStatus
	 */
	public void setApproveStatus(String approveStatus) {
		this.approveStatus = approveStatus;
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
	 * 获取 productOid
	 */
	public String getProductOid() {
		return productOid;
	}

	/**
	 * 设置 productOid
	 */
	public void setProductOid(String productOid) {
		this.productOid = productOid;
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
	 * 获取 resultOid
	 */
	public String getResultOid() {
		return resultOid;
	}

	/**
	 * 设置 resultOid
	 */
	public void setResultOid(String resultOid) {
		this.resultOid = resultOid;
	}
	
	
}
