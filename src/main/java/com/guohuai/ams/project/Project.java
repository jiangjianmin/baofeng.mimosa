package com.guohuai.ams.project;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.guohuai.ams.investment.Investment;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @ClassName: Project
 * @Description: 底层项目实体
 * @author vania
 * @date 2016年5月16日 上午10:15:56
 *
 */
@Entity
@Table(name = "T_GAM_PROJECT")
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Project extends UUID {

	private static final long serialVersionUID = -8451087996991540133L;

	// 关联投资标的id
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "targetOid", referencedColumnName = "oid")
	@JsonBackReference
	private Investment investment;

	/**
	 * 项目名称
	 */
	private String projectName;
	/**
	 * 项目类型
	 */
	private String projectType;
	/**
	 * 项目类型名称
	 */
	private String projectTypeName;
	/**
	 * 项目经理
	 */
	private String projectManager;
	/**
	 * 城市
	 */
	private String projectCity;
	/**
	 * 资金方所在地
	 */
	private String funderAddr;
	/**
	 * 资金用途
	 */
	private String fundUsage;
	/**
	 * 项目还款来源
	 */
	private String payment;
	/**
	 * 外部项目增信措施
	 */
	private String trustMeasures;
	/**
	 * 其他外部项目增信措施
	 */
	private String trustMeasuresName;
	/**
	 * 房地产项目所在地
	 */
	private String projectAddr;
	/**
	 * 开发商排名
	 */
	private String developerRank;
	/**
	 * 房地产项目属性
	 */
	private String estateProp;
	/**
	 * 房地产项目属性名称
	 */
	private String estatePropName;
	/**
	 * 完工情况
	 */
	private String estateCompletion;
	/**
	 * 政府层级
	 */
	private String govLevel;
	/**
	 * 是否有担保人
	 */
	private String warrantor;
	/**
	 * 担保人所在地
	 */
	private String warrantorAddr;
	/**
	 * 担保人总资产
	 */
	private BigDecimal warrantorCapital;
	/**
	 * 担保人负债
	 */
	private BigDecimal warrantorDebt;
	/**
	 * 担保方式
	 */
	@Size(max = 32, message = "保证方式oid最大32个字符！")
	private String guaranteeModeOid; // CCPWarrantyModeForm
	@Size(max = 64, message = "保证方式名称最大32个字符！")
	private String guaranteeModeTitle;
	@Digits(integer = 4, fraction = 4, message = "保证方式权重最大4位整数4位小数")
	private BigDecimal guaranteeModeWeight;
	@Size(max = 32, message = "保证方式担保期限oid最大32个字符！")
	private String guaranteeModeExpireOid; // CCPWarrantyModeForm111
	@Size(max = 64, message = "保证方式担保期限名称最大32个字符！")
	private String guaranteeModeExpireTitle;
	@Digits(integer = 4, fraction = 4, message = "保证方式担保期限权重最大4位整数4位小数")
	private BigDecimal guaranteeModeExpireWeight;

	/**
	 * 是否有抵押
	 */
	private String pledge;
	/**
	 * 抵押人
	 */
	private String mortgager;
	/**
	 * 出质人
	 */
	private String pledgor;
	/**
	 * 抵押物所在地
	 */
	private String pledgeAddr;
	/**
	 * 抵押物形态
	 */
	private String pledgeType;
	/**
	 * 数量或面积
	 */
	private String pledgeWeight;
	/**
	 * 抵押物名称/座落
	 */
	private String pledgeName;
	/**
	 * 抵押物评估机构
	 */
	private String pledgeAssessment;
	/**
	 * 评估价值
	 */
	@Digits(integer = 4, fraction = 4, message = "质押方式担保期限权重最大4位整数4位小数")
	private BigDecimal pledgeValuation;
	/**
	 * 抵押顺位
	 */
	private String pledgePriority;
	/**
	 * 抵押率
	 */
	private BigDecimal pledgeRatio;
	/**
	 * 抵押方式
	 */
	@Size(max = 32, message = "抵押方式oid最大32个字符！")
	private String mortgageModeOid; // CCPWarrantyModeForm
	@Size(max = 64, message = "抵押方式名称最大32个字符！")
	private String mortgageModeTitle;
	@Digits(integer = 4, fraction = 4, message = "抵押方式权重最大4位整数4位小数")
	private BigDecimal mortgageModeWeight;
	@Size(max = 32, message = "抵押方式担保期限oid最大32个字符！")
	private String mortgageModeExpireOid; // CCPWarrantyModeForm111
	@Size(max = 64, message = "抵押方式担保期限名称最大32个字符！")
	private String mortgageModeExpireTitle;
	@Digits(integer = 4, fraction = 4, message = "抵押方式担保期限权重最大4位整数4位小数")
	private BigDecimal mortgageModeExpireWeight;

	/**
	 * 是否有质押
	 */
	private String hypothecation;
	/**
	 * 质押方式
	 */
	@Size(max = 32, message = "质押方式oid最大32个字符！")
	private String hypothecationModeOid; // CCPWarrantyModeForm
	@Size(max = 64, message = "质押方式名称最大32个字符！")
	private String hypothecationModeTitle;
	@Digits(integer = 4, fraction = 4, message = "质押方式权重最大4位整数4位小数")
	private BigDecimal hypothecationModeWeight;
	@Size(max = 32, message = "质押方式担保期限oid最大32个字符！")
	private String hypothecationModeExpireOid; // CCPWarrantyModeForm111
	@Size(max = 64, message = "质押方式担保期限名称最大32个字符！")
	private String hypothecationModeExpireTitle;
	@Digits(integer = 4, fraction = 4, message = "质押方式担保期限权重最大4位整数4位小数")
	private BigDecimal hypothecationModeExpireWeight;
	/**
	 * 风险系数
	 */
	private BigDecimal riskFactor;

	/**
	 * 保证金
	 */
	private BigDecimal margin;
	/**
	 * 项目规模
	 */
	private BigDecimal projectScale;
	/**
	 * 融资成本
	 */
	private BigDecimal financeCosts;
	/**
	 * SPV
	 */
	private String spv;
	/**
	 * SPV通道费率
	 */
	private BigDecimal spvTariff;
	/**
	 * 项目评级
	 */
	private String projectGrade;
	/**
	 * 评级机构
	 */
	private String gradeAssessment;
	/**
	 * 评级时间
	 */
	private Timestamp gradeTime;
	private String creator;
	private String operator;
	private Timestamp createTime;
	private Timestamp updateTime;
}
