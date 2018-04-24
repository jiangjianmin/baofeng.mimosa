package com.guohuai.mmp.investor.cashflow;

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
import com.guohuai.mmp.investor.bankorder.InvestorBankOrderEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.coupon.InvestorCouponOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资人-资金变动明细
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_CASHFLOW")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorCashFlowEntity extends UUID implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1622280278625300593L;

	
	/**
	 * 申购(invest expInvest)、赎回(normalRedeem, fastRedeem, normalExpRedeem)、
	 * 退款(refund)、还本付息(cash)四种，其中，活动体验金自动申购，体验金到期自动赎回，体验金收入自动转入活期
	 */
	
	/** 资金变动类型--投资 */
	public static final String CASHFLOW_tradeType_invest = "invest";
	/** 资金变动类型--投资 */
	public static final String CASHFLOW_tradeType_expInvest = "expInvest";
	/** 资金变动类型--冲销单 */
	public static final String CASHFLOW_tradeType_writeOff = "writeOff";
	/** 资金变动类型--普赎 */
	public static final String CASHFLOW_tradeType_normalRedeem = "normalRedeem";
	/** 资金变动类型--天天向上赎回 */
	public static final String CASHFLOW_tradeType_incrementRedeem = "incrementRedeem";
	/** 资金变动类型--申购赎回 */
	public static final String CASHFLOW_tradeType_specialRedeem = "specialRedeem";
	/** 资金变动类型--普赎 */
	public static final String CASHFLOW_tradeType_normalExpRedeem = "normalExpRedeem";
	/** 资金变动类型--快赎 */
	public static final String CASHFLOW_tradeType_fastRedeem = "fastRedeem";
	/** 资金变动类型--清盘赎回 */
	public static final String CASHFLOW_tradeType_clearRedeem = "clearRedeem";
	/** 资金变动类型--募集失败退款 */
	public static final String CASHFLOW_tradeType_refund = "refund";
	/** 资金变动类型--买卖单 */
	public static final String CASHFLOW_tradeType_buy = "buy";
	/** 资金变动类型--充值 */
	public static final String CASHFLOW_tradeType_deposit = "deposit";
	/** 资金变动类型--提现 */
	public static final String CASHFLOW_tradeType_withdraw = "withdraw";
	/** 资金变动类型--还本 */
	public static final String CASHFLOW_tradeType_repayLoan = "repayLoan";
	/** 资金变动类型--付息 */
	public static final String CASHFLOW_tradeType_repayInterest = "repayInterest";
	/** 资金变动类型--还本/付息 */
	public static final String CASHFLOW_tradeType_cash = "cash";
	/** 资金变动类型--手续费 */
	public static final String CASHFLOW_tradeType_fee = "fee";
	/** 资金变动类型--红包提现 */
	public static final String CASHFLOW_tradeType_cashCoupon = "cashCoupon";
	/** 资金变动类型--二级邀请奖励收益*/
	public static final String CASHFLOW_tradeType_profitInvest = "profitInvest";
	/** 资金变动类型--转活*/
	public static final String CASHFLOW_tradeType_noPayInvest = "noPayInvest";
	/** 资金变动类型--快定宝*/
	public static final String CASHFLOW_tradeType_bfPlusRedeem = "bfPlusRedeem";

	/**
	 * 所属投资人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;
	
	/**
	 * 所属交易委托单
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tradeOrderOid", referencedColumnName = "oid")
	private InvestorTradeOrderEntity tradeOrder;
	
	/**
	 * 所属银行委托单
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bankOrderOid", referencedColumnName = "oid")
	private InvestorBankOrderEntity bankOrder;
	
	/**
	 * 所属卡券委托单
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "couponOrderOid", referencedColumnName = "oid")
	private InvestorCouponOrderEntity couponOrder;
	
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
