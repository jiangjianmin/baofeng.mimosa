package com.guohuai.mmp.platform.reserved.account.cashflow;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.platform.reserved.account.ReservedAccountEntity;
import com.guohuai.mmp.platform.reserved.order.ReservedOrderEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-备付金变动明细
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_RESERVEDACCOUNT_CASHFLOW")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ReservedAccountCashFlowEntity extends UUID implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 978504296255503699L;
	
	/** 交易类型--借款 */
	public static final String CASHFLOW_tradeType_borrow = "borrow";
	/** 交易类型--还款 */
	public static final String CASHFLOW_tradeType_return = "return";
	
	/**
	 * 所属备付金
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservedOid", referencedColumnName = "oid")
	private ReservedAccountEntity reservedAccount;
	
	/**
	 * 所属委托单
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orderOid", referencedColumnName = "oid")
	ReservedOrderEntity reservedOrderEntity;
	
	/**
	 * 交易金额
	 */
	private BigDecimal tradeAmount;
	
	/**
	 * 交易类型
	 */
	private String tradeType;
	
	private Timestamp updateTime;
	private Timestamp createTime;
}
