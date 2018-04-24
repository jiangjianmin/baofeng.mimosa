package com.guohuai.mmp.publisher.investor.interest.result;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-投资人-阶段收益明细
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_INVESTOR_INTEREST_RESULT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
@DynamicInsert
@DynamicUpdate
public class InterestResultEntity extends UUID {
	

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6699999771992692479L;
	
	/**
	 *  'ALLOCATED: 发放完成;  ALLOCATEFAIL: 发放失败
	 */
	public static final String RESULT_status_ALLOCATED = "ALLOCATED";
	public static final String RESULT_status_ALLOCATEFAIL = "ALLOCATEFAIL";

	/**
	 * 关联产品
	 */
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	Product product;
	
	/**
	 * 关联收益分配
	 */
	@JoinColumn(name = "allocateOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	IncomeAllocate incomeAllocate;
	
	/**
	 * 收益日
	 */
	Date allocateDate;
	
	/**
	 * 成功分配收益金额
	 */
	BigDecimal successAllocateIncome = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 成功分配基础收益金额
	 */
	BigDecimal successAllocateBaseIncome = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 成功分配奖励收益金额
	 */
	BigDecimal successAllocateRewardIncome = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 未分配金额
	 */
	BigDecimal leftAllocateIncome = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 未分配基础金额
	 */
	BigDecimal leftAllocateBaseIncome = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 未分配奖励金额
	 */
	BigDecimal leftAllocateRewardIncome = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 成功分配投资者数
	 */
	Integer successAllocateInvestors = SysConstant.INTEGER_defaultValue;
	
	/**
	 * 失败分配投资者数
	 */
	Integer failAllocateInvestors = SysConstant.INTEGER_defaultValue;
	
	/**
	 * 状态
	 */
	String status;
	
	String anno;
	
	Timestamp updateTime, createTime;
}
