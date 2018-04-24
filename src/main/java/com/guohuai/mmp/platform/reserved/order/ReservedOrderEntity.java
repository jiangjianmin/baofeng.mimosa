package com.guohuai.mmp.platform.reserved.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.platform.reserved.account.ReservedAccountEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-备付金-委托单
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_RESERVED_ORDER")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ReservedOrderEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = -6172730568411277011L;
	
	/** 交易类型--借款 */
	public static final String ORDER_orderType_borrow = "borrow";
	/** 交易类型--还款 */
	public static final String ORDER_orderType_return = "return";
	/** 交易类型--充值 */
	public static final String ORDER_orderType_deposit = "deposit";
	/** 交易类型--提现 */
	public static final String ORDER_orderType_withdraw = "withdraw";
	
	
	/** 订单状态--待支付 */
	public static final String ORDER_orderStatus_toPay = "toPay";
	/** 订单状态--支付失败 */
	public static final String ORDER_orderStatus_payFailed = "payFailed";
	/** 订单状态--支付成功 */
	public static final String ORDER_orderStatus_paySuccess = "paySuccess";
	/** 订单状态--申请失败 */
	public static final String ORDER_orderStatus_submitFailed = "submitFailed";
	
	//超级户：superAcc，基本户：basicAcc 运营户：operationAcc
	public static final String ORDER_relatedAcc_superAcc = "superAcc";
	public static final String ORDER_relatedAcc_basicAcc = "basicAcc";
	public static final String ORDER_relatedAcc_operationAcc = "operationAcc";
	
	/**
	 * 所属备付金
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservedOid", referencedColumnName = "oid")
	private ReservedAccountEntity reservedAccount;
	
	/**
	 * 订单号
	 */
	private String orderCode;
	
	/**
	 * 交易类型
	 */
	private String orderType;
	
	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount;
	
	/**
	 * 订单状态
	 */
	private String orderStatus;
	
	/**
	 * 订单完成时间
	 */
	private Timestamp completeTime;
	
	/**
	 * 关联账户
	 */
	private String relatedAcc;
	
	private Timestamp updateTime;
	/**
	 * 订单创建时间
	 */
	private Timestamp createTime;
}
