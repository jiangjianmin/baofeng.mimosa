package com.guohuai.mmp.platform.publisher.order;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-发行人轧差-委托单
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_PUBLISHER_ORDER")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PublisherOrderEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = -6172730568411277011L;
	
	/** 交易类型--借款 */
	public static final String ORDER_orderType_borrow = "borrow";
	/** 交易类型--还款 */
	public static final String ORDER_orderType_return = "return";
	
	/** 订单状态--待支付 */
	public static final String ORDER_orderStatus_toPay = "toPay";
	/** 订单状态--支付失败 */
	public static final String ORDER_orderStatus_payFailed = "payFailed";
	/** 订单状态--支付成功 */
	public static final String ORDER_orderStatus_paySuccess = "paySuccess";
	/** 订单状态--申请失败 */
	public static final String ORDER_orderStatus_submitFailed = "submitFailed";

	/**
	 * 所属发行人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisherOid", referencedColumnName = "oid")
	private PublisherBaseAccountEntity publisher;
	
	/**
	 * 所属发行人轧差
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "offsetOid", referencedColumnName = "oid")
	private PublisherOffsetEntity offset;
	
	/**
	 * 订单号
	 */
	private String orderCode;
	
	/**
	 * 交易类型
	 */
	private String orderType;
	
	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount;
	
	/**
	 * 订单状态
	 */
	private String orderStatus;
	
	/**
	 * 订单完成时间
	 */
	private Timestamp completeTime;
	
	private Timestamp updateTime;
	/**
	 * 订单创建时间
	 */
	private Timestamp createTime;
}
