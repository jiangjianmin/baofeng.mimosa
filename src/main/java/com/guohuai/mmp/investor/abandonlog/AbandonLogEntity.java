package com.guohuai.mmp.investor.abandonlog;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台_支付_调用日志
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_ORDER_ABANDONLOG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class AbandonLogEntity extends UUID {
	
	private static final long serialVersionUID = 2425452922159934702L;
	
	/**
	 * 原始订单号
	 */
	private String originalOrderCode;
	
	/**
	 * 退款订单号
	 */
	private String refundOrderCode;
	
	/**
	 * 备注
	 */
	private String remark;
	
	private Timestamp createTime;
	private Timestamp updateTime;
	
}
