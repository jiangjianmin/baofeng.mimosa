package com.guohuai.mmp.investor.referprofit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @ClassName: ProfitRuleEntity
 * @Description: 二级邀请--奖励收益明细
 * @author yihonglei
 * @date 2017年6月13日 下午3:09:29
 * @version 1.0.0
 */
@Entity
@Table(name = "T_MONEY_PROFIT_RULE")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProfitRuleEntity implements Serializable{
	private static final long serialVersionUID = -4451342817621258412L;
	
	/**
	 * 主键
	 */
	@Id
	protected String oid;
	/**
	 * 一级邀请人奖励比例
	 */
	private BigDecimal firstFactor;
	/**
	 * 二级邀请人奖励比例
	 */
	private BigDecimal secondFactor;
	/**
	 * 投资人奖励比例
	 */
	private BigDecimal investorFactor;
	/**
	 * 活期奖励比例
	 */
	private BigDecimal demandFactor;
	/**
	 * 定期奖励比例
	 */
	private BigDecimal depositFactor;
	/**
	 * 修改时间
	 */
	private Date updateTime;
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	
}
