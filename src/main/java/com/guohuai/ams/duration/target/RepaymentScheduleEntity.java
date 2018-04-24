package com.guohuai.ams.duration.target;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

/**
 * 投资标的 - 还款计划
 * @author star.zhu
 * 2016年6月24日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_REPAYMENT")
@DynamicInsert
@DynamicUpdate
public class RepaymentScheduleEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 类型
	 */
	public static final String interest 	= "利息兑付";
	public static final String capital 		= "本息兑付";
	
	/**
	 * 状态
	 */
	public static final String unPay 	= "未支付";
	public static final String pay 		= "已支付";

	@Id
	private String oid;
	// 持仓oid
	private String holdOid;
	// 资产池id
	private String assetPoolOid;
	// 投资标的oid
	private String targetOid;
	// 投资标的名称
	private String targetName;
	// 还款期数
	private int seq;
	// 还款日期
	private Date repaymentDate;
	// 还款金额
	private BigDecimal repaymentAmount;
	// 类型（利息支付，本金兑付）
	private String type;
	// 状态（已支付，未支付）
	private String status;
	
	private String creator;
	private Timestamp createTime;
	private String operator;
	private Timestamp updateTime;
}
