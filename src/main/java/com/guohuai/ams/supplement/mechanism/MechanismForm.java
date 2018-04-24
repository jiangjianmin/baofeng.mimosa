package com.guohuai.ams.supplement.mechanism;


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
public class MechanismForm {
	
	private String fullName;//机构全称
	private String shotName;//机构简称
	private String contactMan;//联系人
	private String contactPhone;//联系方式
	private String account;//账户
	private String bankName;//银行名
	private String accountBank;//开户行
	private String remark;//备注

}
