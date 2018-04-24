package com.guohuai.mmp.investor.bankorder;

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
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资人-银行委托单
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_BANKORDER")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorBankOrderEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = -5183984182066265200L;

	/** 交易类型--充值 */
	public static final String BANKORDER_orderType_deposit = "deposit";
	/** 交易类型--提现 */
	public static final String BANKORDER_orderType_withdraw = "withdraw";

	
	/** 手续费支付方--平台 */
	public static final String BANKORDER_feePayer_platform = "platform";
	/** 手续费支付方--用户 */
	public static final String BANKORDER_feePayer_user = "user";

	
	/** 订单状态--已申请 */
	public static final String BANKORDER_orderStatus_submitted = "submitted";
	/** 订单状态--申请失败 */
	public static final String BANKORDER_orderStatus_submitFailed = "submitFailed";
//	/** 订单状态--已拒绝 */
//	public static final String BANKORDER_orderStatus_refused = "refused";
	/** 订单状态--待支付 */
	public static final String BANKORDER_orderStatus_toPay = "toPay";
	/** 订单状态--支付失败 */
	public static final String BANKORDER_orderStatus_payFailed = "payFailed";
	/** 订单状态--支付成功 */
	public static final String BANKORDER_orderStatus_done = "done";

	/**
	 * 所属投资人
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
	 * 手续费支付方
	 */
	private String feePayer;

	/**
	 * 手续费
	 */
	private BigDecimal fee = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 订单状态
	 */
	private String orderStatus;

	/**
	 * 订单创建时间
	 */
	private Timestamp createTime;
	
	/**
	 * 订单时间
	 */
	private Timestamp orderTime;
	/**
	 * 订单完成时间
	 */
	private Timestamp completeTime;

	private Timestamp updateTime;
}
