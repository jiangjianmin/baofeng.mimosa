package com.guohuai.mmp.publisher.investor.levelincome;

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

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-投资人-阶段收益明细
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class LevelIncomeEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = -4739006447628131070L;
	
	/**
	 * 所属持有人手册	
	 */
	@JoinColumn(name = "holdOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private PublisherHoldEntity publisherHold;
	
	/**
	 * 所属产品
	 */
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private Product product;
	/**
	 * 所属奖励规则
	 */
	@JoinColumn(name = "rewardRuleOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private ProductIncomeReward reward;
	/**
	 * 所属投资人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;

	/**
	 * 所属合仓明细
	 */
	@JoinColumn(name = "holdIncomeOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private InvestorIncomeEntity InvestorIncome;
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
	BigDecimal accureVolume = BigDecimal.ZERO;
	
	BigDecimal value = BigDecimal.ZERO;
	/**
	 * 确认日期
	 */
	Date confirmDate;
	
	Timestamp updateTime, createTime;
}
