package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资人-交易委托单
 * 
 * @author yuechao
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestorTradeOrderRep extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = 4333179226422640561L;

	private String orderCode;
	private String memberId;
	private String orderType;
	private String orderStatus;
	private BigDecimal orderAmount;
	
}
