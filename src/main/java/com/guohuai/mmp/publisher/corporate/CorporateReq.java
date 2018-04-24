package com.guohuai.mmp.publisher.corporate;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class CorporateReq {

	/**
	 * 账号
	 */
	private String account; 
	/**
	 * 企业简称
	 */
	private String name;
	/**
	 * 简称拼音
	 */
	private String phonetic;
	/**
	 * 企业全称
	 */
	private String companyName;
	/**
	 * 企业网址
	 */
	private String website;
	/**
	 * 企业地址
	 */
	private String address;
	/**
	 * 联系人
	 */
	private String contact;
	/**
	 * 联系电话
	 */
	private String telephone;
	/**
	 * 联系邮箱
	 */
	private String email;
	/**
	 * 营业执照号
	 */
	private String licenseNo;
	/**
	 * 执照所在地
	 */
	private String licenseAddress;
	/**
	 * 营业期限
	 */
	private String licenseExpireDate;
	/**
	 * 营业范围
	 */
	private String businessScope;
	/**
	 * 组织机构代码
	 */
	private String organizationNo;
	/**
	 * 企业简介
	 */
	private String summary;
	/**
	 * 企业法人
	 */
	private String legalPerson;
	/**
	 * 法人身份证号
	 */
	private String certNo;
	private String certType;
	/**
	 * 法人手机号
	 */
	private String legalPersonPhone;
	/**
	 * 开户银行
	 */
	private String bankCode;
	/**
	 * 开户行省份
	 */
	private String province;
	/**
	 * 开户行城市
	 */
	private String city;
	/**
	 * 银行卡号
	 */
	private String bankAccountNo;
	/**
	 * 支行名称
	 */
	private String bankBranch;
	/**
	 * 附件压缩包名称(简称拼音.zip)
	 */
	private String fileName;
	private String digest;
	private String digestType;
	private String cardType;
	private String cardAttribute;
	
	String[] userOids;
	
	
}
