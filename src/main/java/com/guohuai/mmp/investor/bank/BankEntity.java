package com.guohuai.mmp.investor.bank;

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

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
/**
 * 投资者-银行卡
 * @author xjj
 *
 */
@Entity
@Table(name = "T_MONEY_INVESTOR_BANK")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class BankEntity extends UUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** 关联投资者 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="investorOid", referencedColumnName="oid")
	private InvestorBaseAccountEntity investorBaseAccount;
	
	/** 姓名 */
	private String name;
	
	/** 身份证号 */
	private String idCard;
	
	/** 银行名称 */
	private String bankName;
	
	/** 银行卡号	 */
	private String debitCard;
	
	/** 银行预留手机号 */
	private String phoneNo;
	
	/** createTime */
	private Timestamp createTime;
	
	/** updateTime */
	private Timestamp updateTime;
}
