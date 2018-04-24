package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TradeOrderDetailRep extends BaseRep {
	
	/**
	 * 订单号
	 */
	private String orderCode;
	
	/**
	 * 交易类型
	 */
	private String orderType;
	private String orderTypeDisp;
	
	/**
	 * 订单金额
	 */
	private BigDecimal orderAmount;
	
	/**
	 * 订单份额
	 */
	private BigDecimal orderVolume;	
	
	/**
	 * 订单状态
	 */
	private String orderStatus;
	private String orderStatusDisp;
	
	/**
	 * 手续费支付方
	 */
	private String feePayer;
	private String feePayerDisp;
	
	/**
	 * 手续费
	 */
	private BigDecimal payFee;
	
	/**
	 * 订单创建人
	 */
	 private String createMan;
	 private String createManDisp;
	 
	 /**
	  * 订单创建时间
	  */
	 private Timestamp createTime;
	 
	 /**
	  * 订单完成时间
	  */
	 private Timestamp completeTime;
	 
	 
}
