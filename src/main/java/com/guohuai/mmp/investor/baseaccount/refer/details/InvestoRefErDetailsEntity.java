package com.guohuai.mmp.investor.baseaccount.refer.details;

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
import com.guohuai.mmp.investor.baseaccount.referee.InvestorRefEreeEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestoRefErDetailsEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = -3451433312518067829L;

	/**
	 * 用户-资金账户-推荐人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "refereeOid", referencedColumnName = "oid")
	private InvestorRefEreeEntity investorRefEree;

	/**
	 * 所属投资人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investorOid", referencedColumnName = "oid")
	private InvestorBaseAccountEntity investorBaseAccount;

	private Timestamp createTime;
	
	private Timestamp updateTime;
}
