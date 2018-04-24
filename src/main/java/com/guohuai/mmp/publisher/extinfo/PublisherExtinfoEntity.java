package com.guohuai.mmp.publisher.extinfo;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-扩展信息
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_EXTINFO")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PublisherExtinfoEntity extends UUID {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1751700297118312788L;
	/** 账户类型--虚拟账户 */
	public static final String EXTINFO_accountType_UPSPLAT = "UPSPLAT";
	/** 账户类型--银行结算账户 */
	public static final String EXTINFO_accountType_BNKCARD = "BNKCARD";

	/**
	 * 所属发行人
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisherOid", referencedColumnName = "oid")
	private PublisherBaseAccountEntity publisherBaseAccount;
	
	/**
	 * 公司简称
	 */
	private String name;
	
	/**
	 * 公司名称
	 */
	private String companyName;
	
	/**
	 * 账户类型
	 */
	private String accountType;
	/**
	 * 账户账号
	 */
	private String accountNo;
	/**
	 * 企业网址
	 */
	private String website;
	/**
	 * 企业地址
	 */
	private String address;
	/**
	 * 银行名称
	 */
	private String bankName;
	/**
	 * 银行账号
	 */
	private String bankAccount;
	/**
	 * 联系人
	 */
	private String contact;
	/**
	 * 联系电话
	 */
	private String telephone;
	/**
	 * 联系Email
	 */
	private String email;
	
	/**
	 * 营业执照
	 */
	private String licenceNo;

	private Timestamp updateTime;

	private Timestamp createTime;
}
