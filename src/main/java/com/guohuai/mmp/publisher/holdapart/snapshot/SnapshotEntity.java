package com.guohuai.mmp.publisher.holdapart.snapshot;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-投资人-分仓快照
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_INVESTOR_HOLD_SNAPSHOT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class SnapshotEntity extends UUID {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -934419153790296513L;

	/**
	 * 所属订单
	 */
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "orderOid", referencedColumnName = "oid")
//	private InvestorTradeOrderEntity order;
	private String orderOid;
	
	/**
	 * 所属投资人
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;
	
	/**
	 * 所属合仓
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "holdOid", referencedColumnName = "oid")
	private PublisherHoldEntity hold;
	
	/**
	 * 所属产品
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	private Product product;
	
	/**
	 * 持仓天数
	 */
	private Integer holdDays;
	
	/**
	 * 计息快照份额
	 */
	private BigDecimal snapshotVolume = BigDecimal.ZERO;
	
	/**
	 * 可赎回状态
	 */
	private String redeemStatus;
	
	/**
	 * 快照日期
	 */
	private Date snapShotDate;
	
	/**
	 * 基础收益
	 */
	private BigDecimal baseIncome= BigDecimal.ZERO;
	
	/**
	 * 奖励收益
	 */
	private BigDecimal rewardIncome= BigDecimal.ZERO;
	
	/**
	 * 持仓收益
	 */
	private BigDecimal holdIncome= BigDecimal.ZERO;
	
	/**
	 * 奖励规则
	 */
	private String rewardRuleOid;
	
	/**
	 * 奖励万份收益率
	 */
	private BigDecimal rewardIncomeRatio= BigDecimal.ZERO;
	
	Timestamp updateTime;
	Timestamp createTime;
	
}
