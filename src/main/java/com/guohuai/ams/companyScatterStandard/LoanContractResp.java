package com.guohuai.ams.companyScatterStandard;

import com.guohuai.component.web.view.BaseRep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoanContractResp extends BaseRep {

	private static final long serialVersionUID = 5615896690335617045L;

	/**
	 * 标的编号
	 */
	@Id
	private String code;

	/**
	 * 标的名称
	 */
	private String name;

	/**
	 * 借款期限【单位月】
	 */
	private String loanPeriod;

	/**
	 * 借款金额
	 */
	private String loanVolume;

	/**
	 * 借款利率
	 */
	private String loanRatio;

	/**
	 * 借款用途
	 */
	private String loanUsage;

	/**
	 * 还款方式
	 */
	private String refundMode;

	/**
	 * 组织机构编码
	 */
	private String orgCode;

	/**
	 * 企业名称
	 */
	private String orgName;

	/**
	 * 企业法人
	 */
	private String orgCorporationName;

	/**
	 * 借款机构注册资本
	 */
	private String registeredCapital;

	/**
	 * 借款机构成立时间
	 */
	private String setupDate;

	/**
	 * 借款机构地址
	 */
	private String orgAddress;

	private Timestamp createTime;

	private Timestamp updateTime;


}
