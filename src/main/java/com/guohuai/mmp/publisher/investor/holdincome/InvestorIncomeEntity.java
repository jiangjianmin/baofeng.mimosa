package com.guohuai.mmp.publisher.investor.holdincome;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
/**
 * 发行人-投资人-合仓收益明细
 * 
 * @author xjj
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorIncomeEntity extends UUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 所属持有人手册
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "holdOid", referencedColumnName = "oid")
	private PublisherHoldEntity publisherHold;
	/**
	 * 所属产品
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	private Product product;

	/**
	 * 所属投资人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;
	
	/**
	 * 所属总收益
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "incomeOid", referencedColumnName = "oid")
	private IncomeAllocate incomeAllocate;
	
	
	/**
	 * 收益金额
	 */
	private BigDecimal incomeAmount = BigDecimal.ZERO;	
	
	/**
	 * 基础收益
	 */
	private BigDecimal baseAmount = BigDecimal.ZERO;
	
	/**
	 * 奖励收益
	 */
	private BigDecimal rewardAmount = BigDecimal.ZERO;
	/**
	 * 计息份额
	 */
	private BigDecimal accureVolume = BigDecimal.ZERO;
	/**
	 * 确认日期
	 */
	private Date confirmDate;
	
	private Timestamp updateTime;
	private Timestamp createTime;
	
}
