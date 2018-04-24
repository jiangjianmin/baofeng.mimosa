package com.guohuai.ams.product;

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
 * @ClassName: ProductRaisingIncomeEntity
 * @Description: 定期募集期收益明细
 * @author yihonglei
 * @date 2017年6月13日 下午3:48:18
 * @version 1.0.0
 */
@Entity
@Table(name = "T_MONEY_PRODUCT_RAISING_INCOME")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProductRaisingIncomeEntity implements Serializable{
	private static final long serialVersionUID = -7124007996765019845L;
	
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
	 * 产品 id
	 */
	private String productOid;
	/**
	 * 投资订单id
	 */
	private String orderOid;
	/**
	 * 收益金额
	 */
	private BigDecimal incomeAmount;
	/**
	 * 存续期收益率
	 */
	private BigDecimal recPeriodExpAnYield;
	/**
	 * 发放状态(toClose:待结算，closed:已结算)
	 */
	private String status;
	/**
	 * 更新时间
	 */
	private Date updateTime;
	/**
	 * 创建时间
	 */
	private Date createTime;
	
}
