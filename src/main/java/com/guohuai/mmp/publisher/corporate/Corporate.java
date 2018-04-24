package com.guohuai.mmp.publisher.corporate;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "T_MONEY_CORPORATE")
@lombok.Builder
@lombok.Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Corporate extends UUID {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6460975322081487009L;
	public final static int STATUS_Using = 1; //启用
	public final static int STATUS_Lockin = 2; //锁定
	public final static int AUDIT_STATUS_Auditing = 1; //审核中
	public final static int AUDIT_STATUS_Success = 2; //审核通过
	public final static int AUDIT_STATUS_Failed = 3; //审核失败
	
	public final static String YES = "yes"; //已开户
	public final static String NO = "no"; //没有开户
	
	private String account;
	private String auditOrderNo;
	private String identityId;
	private String identityType;
	private String memberType;
	private String name;
	private String phonetic;
	private String companyName;
	private String logo;
	private String website;
	private String address;
	private String licenseNo;
	private String licenseAddress;
	private String licenseExpireDate;
	private String businessScope;
	private String contact;
	private String telephone;
	private String email;
	private String organizationNo;
	private String summary;
	private String legalPerson;
	private String certNo;
	private String certType;
	private String legalPersonPhone;
	private String bankCode;
	private String cardId;
	private String bankAccountNo;
	private String cardType;
	private String cardAttribute;
	private String province;
	private String city;
	private String bankBranch;
	private String fileName;
	private String digest;
	private String digestType;
	private String extendParam;
	private int auditStatus;
	private String auditMessage;
	private int status;
	private String operator;
	private Timestamp updateTime;
	private Timestamp createTime;
	private String isOpen;
	
	
}
