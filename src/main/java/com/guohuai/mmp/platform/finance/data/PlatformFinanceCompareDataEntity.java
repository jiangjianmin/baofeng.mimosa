package com.guohuai.mmp.platform.finance.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Table;
import com.guohuai.component.persist.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
/**
 * 对账数据
 * @author suzhicheng
 *
 */
@Entity
@Table(name = "T_MONEY_CHECK_COMPAREDATA")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PlatformFinanceCompareDataEntity extends UUID{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4422317274650884893L;
	/** 投资 */
	public static final String ORDERTYPE_INVEST="invest";
	/** 赎回 */
	public static final String ORDERTYPE_REDEEM="redeem";
	
	/** 比对状态  --yes*/
	public static final String CHECKSTATUS_YES="yes";
	/** 比对状态  --no*/
	public static final String CHECKSTATUS_NO="no";
	
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
	/** 创建时间 */
	private Timestamp createTime;
	/** 修改时间 */
	private Timestamp updateTime;
	/** 比对状态 */
	private String checkStatus;
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
	
	
}
