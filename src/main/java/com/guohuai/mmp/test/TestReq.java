package com.guohuai.mmp.test;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import com.guohuai.component.web.parameter.validation.Enumerations;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class TestReq{
	/**
@Null   被注释的元素必须为 null   
@NotNull    被注释的元素必须不为 null   
@AssertTrue     被注释的元素必须为 true   
@AssertFalse    被注释的元素必须为 false   
@Min(value)     被注释的元素必须是一个数字，其值必须大于等于指定的最小值   
@Max(value)     被注释的元素必须是一个数字，其值必须小于等于指定的最大值   
@DecimalMin(value)  被注释的元素必须是一个数字，其值必须大于等于指定的最小值   
@DecimalMax(value)  被注释的元素必须是一个数字，其值必须小于等于指定的最大值   
@Size(max=, min=)   被注释的元素的大小必须在指定的范围内   
@Digits (integer, fraction)     被注释的元素必须是一个数字，其值必须在可接受的范围内   
@Past   被注释的元素必须是一个过去的日期   
@Future     被注释的元素必须是一个将来的日期   
@Pattern(regex=,flag=)  被注释的元素必须符合指定的正则表达式   
    
Hibernate Validator 附加的 constraint   
@NotBlank(message =)   验证字符串非null，且长度必须大于0   
@Email  被注释的元素必须是电子邮箱地址   
@Length(min=,max=)  被注释的字符串的大小必须在指定的范围内   
@NotEmpty   被注释的字符串的必须非空   
@Range(min=,max=,message=)  被注释的元素必须在合适的范围内
	 */
	@NotBlank
	@Pattern(regexp = "^\\d{4}\\-\\d{1,2}\\-\\d{1,2}\\s\\d{2}:\\d{2}:\\d{2}$", message = "成立时间参数错误")
	String name;
	@Pattern(regexp = "^\\d{4}\\-\\d{1,2}\\-\\d{1,2}$", message = "成立时间参数错误")
	
	@NotBlank(message = "成立时间不可为空")
	private String setDate;
	@Enumerations(values = { "0", "1" }, message = "自动下架参数错误")
	private int autoShut;
	@Digits(integer = 4, fraction = 4, message = "预期年化收益参数错误")
	@Length(max = 10, min = 1)
	@NotBlank(message = "预期年化收益不可为空")
	private String expAror;
	@Digits(integer = 4, fraction = 4, message = "预期年化收益参数错误")
	private String expArorSec;
	@Range(min = 1, max = 240, message = "资产期限参数错误")
	@Digits(integer = 4, fraction = 0, message = "资产期限参数错误")
	private int life;
	@Enumerations(values = { "YEAR", "MONTH" }, message = "产品期限单位参数错误")
	private String lifeUnit;
	@Digits(integer = 12, fraction = 4, message = "产品规模参数错误")
	private String raiseScope;
	@Enumerations(values = { "10THOUSAND", "MILLION", "100MILLION" }, message = "产品规模单位参数错误")
	private String raiseUnit;
	@NotBlank
	private String typeOid;
	private String typeName;
	@NotBlank
	private String accrualCycleOid;
	@NotEmpty
	private String accrualCycleName;
	private String usages;
	private String risk;
	@Enumerations(values = { "0", "1" }, message = "兜底参数错误")
	private int reveal;
	private String comment;
	@Valid
	private List<SaveFileReq> files;

}
