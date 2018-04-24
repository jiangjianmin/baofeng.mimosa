package com.guohuai.mmp.publisher.cashflow;

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
import com.guohuai.mmp.platform.publisher.order.PublisherOrderEntity;
import com.guohuai.mmp.publisher.bankorder.PublisherBankOrderEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-资金变动明细
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_CASHFLOW")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PublisherCashFlowEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = 1622280278625300593L;
	
	
	
	/** 资金变动类型--充值 */
	public static final String FLOW_tradeType_deposit = "deposit";
	/** 资金变动类型--提现 */
	public static final String FLOW_tradeType_withdraw = "withdraw";
	/** 交易类型--借款 */
	public static final String FLOW_tradeType_borrow = "borrow";
	/** 交易类型--还款 */
	public static final String FLOW_tradeType_return = "return";
	/** 资金变动类型--手续费 */
	public static final String FLOW_tradeType_fee = "fee";


	/**
	 * 所属发行人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisherOid", referencedColumnName = "oid")
	private PublisherBaseAccountEntity publisherBaseAccount;

	/**
	 * 所属轧差委托单
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisherOrderOid", referencedColumnName = "oid")
	private PublisherOrderEntity publisherOrder;

	/**
	 * 所属银行委托单
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bankOrderOid", referencedColumnName = "oid")
	private PublisherBankOrderEntity publisherBankOrder;

	/**
	 * 交易金额
	 */
	private BigDecimal tradeAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 交易类型
	 */
	private String tradeType;

	private Timestamp updateTime;

	private Timestamp createTime;

}
