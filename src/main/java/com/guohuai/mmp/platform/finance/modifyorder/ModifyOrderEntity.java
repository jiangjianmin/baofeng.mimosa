package com.guohuai.mmp.platform.finance.modifyorder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.guohuai.ams.product.Product;
import com.guohuai.basic.component.ext.hibernate.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.platform.finance.check.PlatformFinanceCheckEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_MONEY_PLATFORM_FINANCE_MODIFYORDER")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ModifyOrderEntity extends UUID{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2702347791215804701L;
	
	/** 修改前状态--已申请*/
	public static final String  BEFOREMODIFY_SUBMITTED = "submitted";
	/** 修改前状态--申请失败 */
	public static final String  BEFOREMODIFY_SUBMITFAILED = "submitFailed";
	/** 修改前状态--已拒绝 */
	public static final String  BEFOREMODIFY_REFUSED = "refused";
	/** 修改前状态--待支付 */
	public static final String  BEFOREMODIFY_TOPAY = "toPay";
	/** 修改前状态--支付失败*/
	public static final String  BEFOREMODIFY_PAYFAILED = "payFailed";
	/** 修改前状态--支付成功 */
	public static final String  BEFOREMODIFY_PAYSUCCESS = "paySuccess";
	/** 修改前状态--已受理 */
	public static final String  BEFOREMODIFY_ACCEPTED = "accepted";
	/** 修改前状态--份额已确认 */
	public static final String  BEFOREMODIFY_CONFIRMED = "confirmed";
	/** 修改前状态--份额确认失败 */
	public static final String  BEFOREMODIFY_CONFIRMFAILED = "confirmFailed";
	/** 修改前状态--交易成功 */
	public static final String  BEFOREMODIFY_DONE = "done";

	/** 修改后状态--已申请 */
	public static final String  AFTERMODIFY_SUBMITTED = "submitted";
	/** 修改后状态--申请失败 */
	public static final String  AFTERMODIFY_SUBMITFAILED = "submitFailed";
	/** 修改后状态--已拒绝 */
	public static final String  AFTERMODIFY_REFUSED = "refused";
	/** 修改后状态--待支付 */
	public static final String  AFTERMODIFY_TOPAY = "toPay";
	/** 修改后状态--支付失败 */
	public static final String  AFTERMODIFY_PAYFAILED = "payFailed";
	/** 修改后状态--支付成功 */
	public static final String  AFTERMODIFY_PAYSUCCESS = "paySuccess";
	/** 修改后状态--已受理 */
	public static final String  AFTERMODIFY_ACCEPTED = "accepted";
	/** 修改后状态--份额已确认 */
	public static final String  AFTERMODIFY_CONFIRMED = "confirmed";
	/** 修改后状态--份额确认失败 */
	public static final String  AFTERMODIFY_CONFIRMFAILED = "confirmFailed";
	/** 修改后状态--交易成功 */
	public static final String  AFTERMODIFY_DONE = "done";
	
	/** 审核状态-- 待审批 */
	public static final String APPROVESTATUS_TOAPPROVE="toApprove";
	/** 审核状态-- 通过 */
	public static final String APPROVESTATUS_PASS="pass";
	/** 审核状态-- 驳回 */
	public static final String APPROVESTATUS_REFUSED="refused";
	
	/** 操作类型-- 补单 */
	public static final String OPTYPE_FIXORDER="fixOrder";
	/** 操作类型-- 废单平仓 */
	public static final String OPTYPE_DISCARDHOLD="discardHold";
	/** 操作类型-- 废单退款 */
	public static final String OPTYPE_DISCARDREFUND="discardRefund";
	/** 操作类型-- 退款 */
	public static final String OPTYPE_REFUND="refund";
	
	/** 交易类型-- 投资 */
	public static final String TRADETYPE_INVEST="invest";
	/** 交易类型-- 赎回 */
	public static final String TRADETYPE_REDEEM="redeem";
	
	/** 处理结果-待处理 */
	public static final String DEALSTATUS_TODEAL="toDeal";
	/** 处理结果-处理中 */
	public static final String DEALSTATUS_DEALING="dealing";
	/** 处理结果-已处理 */
	public static final String DEALSTATUS_DEALT="dealt";
	
	/** 所属对账批次 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "checkOid", referencedColumnName = "oid")
	private PlatformFinanceCheckEntity financeCheck;
	
	/**
	 * 所属产品
	 */
	private String productOid;
	
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

	/**
	 * 获取 financeCheck
	 */
	public PlatformFinanceCheckEntity getFinanceCheck() {
		return financeCheck;
	}

	/**
	 * 设置  financeCheck
	 */
	public void setFinanceCheck(PlatformFinanceCheckEntity financeCheck) {
		this.financeCheck = financeCheck;
	}
	
}
