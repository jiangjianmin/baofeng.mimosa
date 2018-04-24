package com.guohuai.mmp.investor.bankorder;

import java.math.BigDecimal;
import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的充值提现记录 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyBankOrderRep {

	public MyBankOrderRep(String orderOid, String orderType, BigDecimal orderAmount, Date orderTime,
			String orderStatus) {
		this.oid = orderOid;
		this.oType = orderType;
		this.amt = orderAmount;
		this.time = orderTime;
		this.status = orderStatus;
	}

	/** 订单流水号 */
	private String oid;
	/** 订单类型 */
	private String oType;
	/** 订单金额 */
	private BigDecimal amt;
	/** 订单时间 */
	private Date time;
	/** 订单状态 */
	private String status;
}
