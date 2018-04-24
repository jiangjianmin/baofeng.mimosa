package com.guohuai.mmp.publisher.investor.holdapartincome;

import java.io.Serializable;
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
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeEntity;
import com.guohuai.mmp.publisher.investor.levelincome.LevelIncomeEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-投资人-分仓收益明细
 * 
 * @author xjj
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_INVESTOR_INCOME")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PartIncomeEntity extends UUID implements Serializable{
	
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
	 * 所属合仓收益
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "holdIncomeOid", referencedColumnName = "oid")
	private InvestorIncomeEntity holdIncome;
	
	/**
	 * 所属奖励规则
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rewardRuleOid", referencedColumnName = "oid")
	private ProductIncomeReward reward;
	
	/**
	 * 所属阶段收益
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "levelIncomeOid", referencedColumnName = "oid")
	private LevelIncomeEntity levelIncome;
	
	/**
	 * 所属订单
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orderOid", referencedColumnName = "oid")
	private InvestorTradeOrderEntity order;
	
	/**
	 * 收益金额
	 */
	private BigDecimal incomeAmount = BigDecimal.ZERO;
	
	/**
	 * 基础收益
	 */
	private BigDecimal baseAmount = BigDecimal.ZERO;
	
	/**
	 * 奖励金额
	 */
	BigDecimal rewardAmount = BigDecimal.ZERO;
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
