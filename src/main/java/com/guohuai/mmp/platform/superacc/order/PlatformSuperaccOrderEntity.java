package com.guohuai.mmp.platform.superacc.order;

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
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-基本账户
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_SUPERACC_ORDER")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PlatformSuperaccOrderEntity extends UUID {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8147041706130960276L;
	
	
	// orderStatus          varchar(30) comment 'refused:已拒绝，toPay:待支付，payFailed:支付失败，paySuccess:支付成功',
	public static final String ORDER_orderStatus_paySuccess = "paySuccess";

	/** 交易类型--借款 */
	public static final String ORDER_orderType_borrow = "borrow";
	/** 交易类型--还款 */
	public static final String ORDER_orderType_return = "return";
	
	/** 关联账户--超级户 */
	public static final String ORDER_relatedAcc_superAcc = "superAcc";
	
	/**
	 * 所属平台
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "platformOid", referencedColumnName = "oid")
	private PlatformBaseAccountEntity platformBaseAccount;
	
	/**
	 * 所属超级用户
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;
	
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
	private Timestamp createTime;
	
}
