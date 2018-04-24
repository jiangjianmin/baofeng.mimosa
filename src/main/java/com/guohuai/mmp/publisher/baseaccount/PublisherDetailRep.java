package com.guohuai.mmp.publisher.baseaccount;

import java.sql.Timestamp;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.publisher.corporate.Corporate;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PublisherDetailRep extends BaseRep {
	
	public PublisherDetailRep(Corporate corporate) {
		this.oid = corporate.getOid();
		if(corporate.getStatus()==1) {
			this.status = "normal";
		} else {
			this.status = "disabled";
		}
		this.updateTime = corporate.getUpdateTime();
		this.createTime = corporate.getCreateTime();
		this.name = corporate.getName();
		this.companyName = corporate.getCompanyName();
		this.website = corporate.getWebsite();
		this.address = corporate.getAddress();
		this.contact = corporate.getContact();
		this.telephone = corporate.getTelephone();
		this.email =  corporate.getEmail();
		this.licenceNo = corporate.getLicenseNo();
		this.accountType = corporate.getIdentityType();
		this.accountNo = corporate.getAccount();
		this.bankName = corporate.getBankBranch();
		this.bankAccount = corporate.getBankAccountNo();
	}
	
	private String oid;
	// 公司简称
	private String name;
	// 公司名称
	private String companyName;
	// 账户类型
	private String accountType;
	// 账户账号
	private String accountNo;
	// 企业网址
	private String website;
	// 企业地址
	private String address;
	// 联系人
	private String contact;
	// 联系电话
	private String telephone;
	// 联系Email
	private String email;
	// 发行人状态(normal:正常,disabled:禁用)
	private String status;
	// 银行名称
	private String bankName;
	// 银行账户
	private String bankAccount;
	// 营业执照
	private String licenceNo;
	// 更新时间
	private Timestamp updateTime;
	// 创建时间
	private Timestamp createTime;

}
