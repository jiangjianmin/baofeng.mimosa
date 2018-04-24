package com.guohuai.ams.supplement.mechanism;

import java.util.Date;

import com.guohuai.ams.supplement.mechanism.Mechanism.MechanismBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class MechanismRep {
	
	public MechanismRep(Mechanism m){
		this.oid=m.getOid();
		this.fullName=m.getFullName();
		this.shotName=m.getShotName();
		this.contactMan=m.getContactMan();
		this.contactPhone=m.getContactPhone();
		this.account=m.getAccount();
		this.bankName=m.getBankName();
		this.accountBank=m.getAccountBank();
		this.remark=m.getRemark();
		this.status=m.getStatus();
		this.operator=m.getOperator();
		this.createTime=m.getCreateTime();
	}
	private String oid;
	private String fullName;//机构全称
	private String shotName;//机构简称
	private String contactMan;//联系人
	private String contactPhone;//联系方式
	private String account;//账户
	private String bankName;//银行名
	private String accountBank;//开户行
	private String remark;//备注
	private String status;//状态
	private String operator;//操作人
	private Date createTime;//创建时间

}
