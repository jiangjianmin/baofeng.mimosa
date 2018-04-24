package com.guohuai.mmp.platform.reserved.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-备付金账户
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_RESERVEDACCOUNT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ReservedAccountEntity extends UUID implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1665633825395755240L;

	/**
	 * 所属平台
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "platformOid", referencedColumnName = "oid")
	private PlatformBaseAccountEntity platformBaseAccount;

	/**
	 * 三方支付账号
	 */
	private String reservedId;

	/**
	 * 余额
	 */
	private BigDecimal balance;

	/**
	 * 累计充值总额
	 */
	private BigDecimal totalDepositAmount;

	/**
	 * 累计提现总额
	 */
	private BigDecimal totalWithdrawAmount;

	/**
	 * 超级户借款金额
	 */
	private BigDecimal superAccBorrowAmount;
	
	/**
	 * 基本户借款金额
	 */
	private BigDecimal basicAccBorrowAmount;
	
	/**
	 * 运营户账号
	 */
	private String operationId;
	
	/***
	 * 运营户借款金额
	 */
	private BigDecimal operationAccBorrowAmount;

	/**
	 * 最近借款时间
	 */
	private Timestamp lastBorrowTime;
	/**
	 * 最近还款时间
	 */
	private Timestamp lastReturnTime;

	private Timestamp updateTime;
	private Timestamp createTime;
}
