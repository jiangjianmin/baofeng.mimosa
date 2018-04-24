package com.guohuai.ams.duration.order.trust;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

/**
 * 投资标的--本息兑付订单
 * @author star.zhu
 * 2016年5月17日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_TARGET_INCOME")
@DynamicInsert
@DynamicUpdate
public class TrustIncomeEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 状态
	 */
	public static final String STATE_FAIL			= "-2";
	public static final String STATE_AUDIT			= "-1";
	public static final String STATE_APPOINTMENT	= "0";
	public static final String STATE_CONFIRM		= "1";
	public static final String STATE_SUCCESS 		= "2";
	
	public TrustIncomeEntity() {
		this.incomeRate 		= BigDecimalUtil.init0;
		this.income 			= BigDecimalUtil.init0;
		this.auditIncomeRate 	= BigDecimalUtil.init0;
		this.investIncomeRate 	= BigDecimalUtil.init0;
		this.auditIncome 		= BigDecimalUtil.init0;
		this.investIncome 		= BigDecimalUtil.init0;
		this.auditCapital 		= BigDecimalUtil.init0;
		this.investCapital 		= BigDecimalUtil.init0;
		this.capital 			= BigDecimalUtil.init0;
	}

	@Id
	private String oid;
	// 关联资产池投资标的
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "targetOid", referencedColumnName = "oid")
	private TrustEntity trustEntity;
	// 兑付期数
	private Integer seq;
	// 实际收益率
	private BigDecimal incomeRate; 
	// 实际收益
	private BigDecimal income; 
	// 审核收益率
	private BigDecimal auditIncomeRate;
	// 确认收益率
	private BigDecimal investIncomeRate;
	// 审核收益
	private BigDecimal auditIncome;
	// 确认收益
	private BigDecimal investIncome;
	// 审核本金
	private BigDecimal auditCapital;
	// 确认本金
	private BigDecimal investCapital;
	// 本金返还
	private BigDecimal capital;
	// 收益支付日
	private Date incomeDate;
	// 主题评级	
//	private String subjectRating;	
	// 状态（-2：失败，-1：待审核，0：待预约，1：待确认，2：已成立）
	private String state;
	// 申请人
	private String asker;
	// 审核人
	private String auditor; 
	// 确认人
	private String confirmer; 
	// 操作员
	private String operator;
	// UpdateTime
	private Timestamp updateTime;
	// CreateTime
	private Timestamp createTime;
}
