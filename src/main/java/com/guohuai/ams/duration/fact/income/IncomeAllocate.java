package com.guohuai.ams.duration.fact.income;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.product.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 收益分配表
 * @author wangyan
 *
 */
@Entity
@Table(name = "T_GAM_ASSETPOOL_INCOME_ALLOCATE")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class IncomeAllocate implements Serializable {

	private static final long serialVersionUID = -9056237663427594519L;
	

	public static final String ALLOCATE_INCOME_TYPE_raiseIncome = "raiseIncome";//募集期收益
	public static final String ALLOCATE_INCOME_TYPE_durationIncome = "durationIncome";//存续期收益

	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventOid", referencedColumnName = "oid")
	private IncomeEvent incomeEvent;//关联收益分配事件
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productOid", referencedColumnName = "oid")
	private Product product;//关联产品
	private Date baseDate; // 收益基准日
	private BigDecimal capital = new BigDecimal(0);//产品总规模
	private BigDecimal allocateIncome = new BigDecimal(0);//基础收益分配
	private BigDecimal rewardIncome = new BigDecimal(0);//奖励收益分配
	private BigDecimal ratio = new BigDecimal(0);//收益率(年化)	
	private BigDecimal wincome = new BigDecimal(0);//万份收益
	private Integer days = 0;//收益分配天数
	private BigDecimal successAllocateIncome = new BigDecimal(0);//成功分配基础收益金额
	private BigDecimal successAllocateRewardIncome = new BigDecimal(0);//成功分配奖励收益金额
	private BigDecimal leftAllocateIncome = new BigDecimal(0);//剩余总分配收益
	private BigDecimal leftAllocateBaseIncome = new BigDecimal(0);//剩余分配基础金额
	private BigDecimal leftAllocateRewardIncome = new BigDecimal(0);//剩余分配奖励金额
	
	private Integer successAllocateInvestors = 0;//成功分配投资者数
	private Integer failAllocateInvestors = 0;//失败分配投资者数
	
	private String allocateIncomeType;

}
