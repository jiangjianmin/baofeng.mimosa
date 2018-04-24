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
 * @ClassName: ProfitProvideDetailEntity
 * @Description: 二级邀请--奖励发放明细
 * @author yihonglei
 * @date 2017年6月13日 下午2:43:37
 * @version 1.0.0
 */
@Entity
@Table(name = "T_MONEY_PROFIT_PROVIDE_DETAIL")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProfitProvideDetailEntity implements Serializable{
	private static final long serialVersionUID = 5817679377144319024L;
	
	/** 奖励发放明细--发放状态--待结算 */
	public static final String PROFIT_PROVIDE_CloseStatus_toClose = "toClose";
	/** 奖励发放明细--发放状态--结算中 */
	public static final String PROFIT_PROVIDE_CloseStatus_closing = "closing";
	/** 奖励发放明细--发放状态--已结算 */
	public static final String PROFIT_PROVIDE_CloseStatus_closed = "closed";
	
	public static final String SECOND_LEVEN_RANK_DAY = "day";
	public static final String SECOND_LEVEN_RANK_WEEK = "week";
	public static final String SECOND_LEVEN_RANK_MONTH = "month";
	
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
	 * 发放人手机号
	 */
	private String phone;
	/**
	 * 来源人id
	 */
	private String sourceOid;
	/**
	 * 来源人手机号
	 */
	private String sourcePhone;
	/**
	 * 产品类型
	 */
	private String productType;
	/**
	 * 产品id
	 */
	private String productOid;
	/**
	 * 产品名称
	 */
	private String productName;
	/**
	 * 订单id
	 */
	private String orderOid;
	/**
	 * 奖励收益明细id
	 */
	private String profitOid;
	/**
	 * 购买日期
	 */
	private String payDate;
	/**
	 * 发放月度
	 */
	private String provideMonth;
	/**
	 * 发放额度
	 */
	private BigDecimal provideAmount;
	/**
	 * 发放日期
	 */
	private Date provideDate;
	/**
	 * 发放状态
	 */
	private String status;
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	
}
