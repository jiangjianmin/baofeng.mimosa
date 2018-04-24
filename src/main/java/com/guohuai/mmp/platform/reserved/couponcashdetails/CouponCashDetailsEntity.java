package com.guohuai.mmp.platform.reserved.couponcashdetails;

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
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountEntity;
import com.guohuai.mmp.platform.reserved.order.ReservedOrderEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台--备付金-卡券核销明细
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_RESERVED_COUPONCASHDETAILS")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class CouponCashDetailsEntity extends UUID {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8564326932219331057L;
	
	/** 核销状态--待核销 */
	public static final String DETAIL_cashStatus_toCash = "toCash";
	/** 核销状态--核销中*/
	public static final String DETAIL_cashStatus_cashing = "cashing";
	/** 核销状态--已核销*/
	public static final String DETAIL_cashStatus_cashed = "cashed";
	/** 核销状态--核销失败*/
	public static final String DETAIL_cashStatus_cashFailed = "cashFailed";

	/**
	 * 所属平台
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "platformOid", referencedColumnName = "oid")
	private PlatformBaseAccountEntity platformBaseAccount;
	
	/**
	 * 所属备付金订单号
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservedOrderOid", referencedColumnName = "oid")
	private ReservedOrderEntity reservedOrder;
	
	/**
	 * 核销金额
	 */
	private BigDecimal cashAmount;
	
	/**
	 * 订单号
	 */
	private String orderCode;
	
	/**
	 * 卡券编号
	 */
	private String coupons;
	
	/**
	 * 核销状态
	 */
	private String cashStatus;
	
	/**
	 * 核销时间
	 */
	private Timestamp cashTime;
	
	private Timestamp updateTime;
	
	private Timestamp createTime;
}
