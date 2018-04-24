package com.guohuai.ams.project;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectForm implements Cloneable, Serializable {

	/**
	 * @Fields serialVersionUID : TODO
	 */

	private static final long serialVersionUID = 5787252786984515512L;
	private String oid;
	@NotNull(message = "标的id不能为空")
	private String targetOid;
	@NotNull(message = "项目名称不能为空")
	private String projectName;
	@NotNull(message = "项目类型不能为空")
	private String projectType;
	private String projectTypeName;
	@Size(max = 32, message = "项目经理最长32个字符！")
	private String projectManager;
	@Size(max = 128, message = "城市最长128个字符！")
	private String projectCity;
	@Size(max = 128, message = "资金方所在地最长128个字符！")
	private String funderAddr;
	@Size(max = 1024, message = "资金资金用途最长1024个字符！")
	private String fundUsage;
	@Size(max = 1024, message = "项目还款来源最长1024个字符！")
	private String payment;
	@Size(max = 32, message = "外部项目曾信最长32个字符！")
	private String trustMeasures;
	@Size(max = 32, message = "外部项目曾信措施最长32个字符！")
	private String trustMeasuresName;
	@Size(max = 1024, message = "房地产项目所在地最长1024个字符！")
	private String projectAddr;
	@Size(max = 32, message = "开发商排名最长32个字符！")
	private String developerRank;
	@Size(max = 32, message = "房地产项目属性最长32个字符！")
	private String estateProp;
	@Size(max = 32, message = "房地产项目属性名称最长32个字符！")
	private String estatePropName;
	@Size(max = 32, message = "完工情况最长32个字符！")
	private String estateCompletion;
	@Size(max = 64, message = "政府层级最长64个字符！")
	private String govLevel;
	@NotNull(message = "是否有担保人不能为空")
	@Size(max = 32, message = "是否有担保人最长32个字符！")
	private String warrantor;
	@Size(max = 1024, message = "担保人所在地最长1024个字符！")
	private String warrantorAddr;
	@Digits(integer = 10, fraction = 4, message = "担保人总资产最大10位整数4位小数")
	private BigDecimal warrantorCapital;
	@Digits(integer = 10, fraction = 4, message = "担保人负债最大10位整数4位小数")
	private BigDecimal warrantorDebt;
	/**
	 * 担保方式
	 */
	@Size(max = 32, message = "保证方式oid最长32个字符！")
	private String guaranteeModeOid; // CCPWarrantyModeForm
	@Size(max = 64, message = "保证方式名称最长32个字符！")
	private String guaranteeModeTitle;
	@Digits(integer = 4, fraction = 4, message = "保证方式权重最大4位整数4位小数")
	private BigDecimal guaranteeModeWeight;
	@Size(max = 32, message = "保证方式担保期限oid最长32个字符！")
	private String guaranteeModeExpireOid; // CCPWarrantyModeForm111
	@Size(max = 64, message = "保证方式担保期限名称最长32个字符！")
	private String guaranteeModeExpireTitle;
	@Digits(integer = 4, fraction = 4, message = "保证方式担保期限权重最大4位整数4位小数")
	private BigDecimal guaranteeModeExpireWeight;
	
	@NotNull(message = "是否有抵押不能为空")
	@Size(max = 32, message = "是否有抵押最长32个字符！")
	private String pledge;
	@Size(max = 32, message = "抵押人最长32个字符！")
	private String mortgager;
	@Size(max = 32, message = "出质人最长32个字符！")
	private String pledgor;
	@Size(max = 1024, message = "抵押物所在地最长1024个字符！")
	private String pledgeAddr;
	@Size(max = 1024, message = "抵押物形态最长1024个字符！")
	private String pledgeType;
	@Size(max = 64, message = "抵押物数量或者面积最长64个字符！")
	private String pledgeWeight;
	@Size(max = 1024, message = "抵押物名称或者坐落最长1024个字符！")
	private String pledgeName;
	@Size(max = 64, message = "抵押物评估机构最长64个字符！")
	private String pledgeAssessment;	
	@Digits(integer = 10, fraction = 4, message = "抵押物评估价值最大10位整数4位小数")
	private BigDecimal pledgeValuation;
	@Size(max = 32, message = "抵押顺位最长32个字符！")
	private String pledgePriority;
	@Digits(integer = 4, fraction = 4, message = "抵押率最大4位整数4位小数")
	private BigDecimal pledgeRatio;
	/**
	 * 抵押方式
	 */
	@Size(max = 32, message = "抵押方式oid最长32个字符！")
	private String mortgageModeOid; // CCPWarrantyModeForm
	@Size(max = 64, message = "抵押方式名称最长32个字符！")
	private String mortgageModeTitle;
	@Digits(integer = 4, fraction = 4, message = "抵押方式权重最大4位整数4位小数")
	private BigDecimal mortgageModeWeight;
	@Size(max = 32, message = "抵押方式担保期限oid最长32个字符！")
	private String mortgageModeExpireOid; // CCPWarrantyModeForm111
	@Size(max = 64, message = "抵押方式担保期限名称最长32个字符！")
	private String mortgageModeExpireTitle;
	@Digits(integer = 4, fraction = 4, message = "抵押方式担保期限权重最大4位整数4位小数")
	private BigDecimal mortgageModeExpireWeight;
	
	@NotNull(message = "是否有质押不能为空")
	@Size(max = 32, message = "是否有质押最长32个字符！")
	private String hypothecation;
	/**
	 * 质押方式
	 */
	@Size(max = 32, message = "质押方式oid最长32个字符！")
	private String hypothecationModeOid; // CCPWarrantyModeForm
	@Size(max = 64, message = "质押方式名称最长32个字符！")
	private String hypothecationModeTitle;
	@Digits(integer = 4, fraction = 4, message = "质押方式权重最大4位整数4位小数")
	private BigDecimal hypothecationModeWeight;
	@Size(max = 32, message = "质押方式担保期限oid最长32个字符！")
	private String hypothecationModeExpireOid; // CCPWarrantyModeForm111
	@Size(max = 64, message = "质押方式担保期限名称最长32个字符！")
	private String hypothecationModeExpireTitle;
	@Digits(integer = 4, fraction = 4, message = "质押方式担保期限权重最大4位整数4位小数")
	private BigDecimal hypothecationModeExpireWeight;
	/**
	 * 风险系数
	 */
	private BigDecimal riskFactor;
	
	@Digits(integer = 10, fraction = 4, message = "保证金最大10位整数4位小数")	
	private BigDecimal margin;
	@Digits(integer = 10, fraction = 4, message = "项目成本最大10位整数4位小数")
	private BigDecimal projectScale;
	@Digits(integer = 10, fraction = 4, message = "融资成本最大10位整数4位小数")
	private BigDecimal financeCosts;
	@Size(max = 64, message = "SPV最长64个字符！")
	private String spv;
	@Digits(integer = 4, fraction = 4, message = "SPV通道费率最大4位整数4位小数")
	private BigDecimal spvTariff;
	@Size(max = 32, message = "项目评级最长32个字符！")
	private String projectGrade;
	@Size(max = 32, message = "项目评级机构最长32个字符！")
	private String gradeAssessment;
	private Timestamp gradeTime;
	@Size(max = 32, message = "创建人最长32个字符！")
	private String creator;
	@Size(max = 32, message = "操作人最长32个字符！")
	private String operator;
	private Timestamp createTime;
	private Timestamp updateTime;
}
