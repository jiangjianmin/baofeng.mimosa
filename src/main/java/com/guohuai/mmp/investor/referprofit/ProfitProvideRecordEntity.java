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
 * @ClassName: ProfitProvideRecordEntity
 * @Description: 二级邀请--奖励发放记录
 * @author yihonglei
 * @date 2017年6月13日 下午3:17:17
 * @version 1.0.0
 */
@Entity
@Table(name = "T_MONEY_PROFIT_PROVIDE_RECORD")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProfitProvideRecordEntity implements Serializable{
	private static final long serialVersionUID = -1806869207961216052L;
	
	/**
	 * 主键
	 */
	@Id
	protected String oid;
	/**
	 * 发放人id
	 */
	private String provideOid;
	/**
	 * 发放订单id
	 */
	private String orderOid;
	/**
	 * 月度(yyyyMM)
	 */
	private String month;
	/**
	 * 发放额度
	 */
	private BigDecimal provideAmount;
	/**
	 * 发放日期
	 */
	private Date provideDate;
	/**
	 * 创建时间
	 */
	private Date createTime;
	
}
