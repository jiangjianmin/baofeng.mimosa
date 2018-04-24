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
public class ModifyOrderReq implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 215330073282277761L;
	/** 对账批次ID */
	private String checkOid;
	/** 投资者id */
	private String investorOid;
	/** 订单号 */
	private String orderCode;
	/** 产品Oid */
	private String productOid;
	/** 订单金额 */
	private BigDecimal orderAmount=BigDecimal.ZERO;
	/** 交易类型 */
	private String tradeType;
	/** 操作类型 */
	private String opType;
	/** 操作人 */
	private String operator;
	/** 修改前状态 */
	private String premodifyStatus;
	/** 所属对账结果 */
	private String resultOid;
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
