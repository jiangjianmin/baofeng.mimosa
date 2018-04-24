package com.guohuai.mmp.publisher.corporate;

import java.sql.Timestamp;

import com.guohuai.component.web.view.PagesRep;



@lombok.Builder
@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class CorporateQueryRep {
	private String oid;
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
	String isOpen;

	
}
