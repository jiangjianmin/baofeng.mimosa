package com.guohuai.mmp.investor.baseaccount.referee;

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
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户-资金账户-推荐人
 * 
 * @author wanglei
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_BASEACCOUNT_REFEREE")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorRefEreeEntity extends UUID {

	private static final long serialVersionUID = -9146672556764270686L;

	/**
	 * 所属投资人
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;
	
	/** 推荐注册人数 */
	private Integer referRegAmount = SysConstant.INTEGER_defaultValue;
	/** 昨天推荐人数 */
	private Integer yesterdayRecommenders = SysConstant.INTEGER_defaultValue;
	/** 推荐购买人数 */
	private Integer referPurchasePeopleAmount = SysConstant.INTEGER_defaultValue;
	/** 推荐购买产品总额 */
	private BigDecimal referPurchaseMoneyVolume = SysConstant.BIGDECIMAL_defaultValue;

	private Timestamp createTime;
	
	private Timestamp updateTime;

}
