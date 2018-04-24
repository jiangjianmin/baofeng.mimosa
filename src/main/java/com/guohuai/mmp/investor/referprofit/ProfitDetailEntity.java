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
 * @ClassName: ProfitDetailEntity
 * @Description: 二级邀请--奖励收益明细
 * @author yihonglei
 * @date 2017年6月13日 下午2:25:11
 * @version 1.0.0
 */
@Entity
@Table(name = "T_MONEY_PROFIT_DETAIL")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProfitDetailEntity implements Serializable{
	private static final long serialVersionUID = 2212489556475156816L;
	
	/**
	 * 主键
	 */
	@Id
	protected String oid;
	/**
	 * 投资人id
	 */
	private String investorOid;
	/**
	 * 一级邀请人id
	 */
	private String firstOid;
	/**
	 * 二级邀请人id
	 */
	private String secondOid;
	/**
	 * 产品id
	 */
	private String productOid;
	/**
	 * 订单id
	 */
	private String orderOid;
	/**
	 * 产品类型（ PRODUCTTYPE_01：定期  PRODUCTTYPE_02:活期）
	 */
	private String productType;
	/**
	 * 购买日期(活期yyyyMM 定期yyyyMMdd)
	 */
	private String payDate;
	/**
	 * 总收益（元）
	 */
	private BigDecimal totalInterest;
	/**
	 * 创建时间
	 */
	private Date createTime;
	
}
