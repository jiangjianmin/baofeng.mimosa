package com.guohuai.ams.companyScatterStandard;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *
 *借款合同实体
 * @author yujianlong
 * @date 2018/4/21 12:20
 * @param
 * @return
 */
@Entity
@Table(name = "T_GAM_LOAN_CONTRACT")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class LoanContract implements Serializable {

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
	private Integer loanPeriod = 0;

	/**
	 * 借款金额
	 */
	private BigDecimal loanVolume = new BigDecimal(0);

	/**
	 * 借款利率
	 */
	private BigDecimal loanRatio = new BigDecimal(0);

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
