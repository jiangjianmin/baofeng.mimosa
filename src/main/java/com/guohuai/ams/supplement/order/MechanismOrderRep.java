package com.guohuai.ams.supplement.order;

import java.math.BigDecimal;
import java.util.Date;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class MechanismOrderRep {

	public MechanismOrderRep(MechanismOrder o) {
		this.orderCode = o.getOrderCode();
		this.orderStatus = o.getOrderStatus();
		this.orderAmount = o.getOrderAmount();
		this.orderType = o.getOrderType();
		this.productName = o.getProduct().getName();
		this.mechanismFullName = o.getMechanism().getFullName();
		this.fictitiousTime = o.getFictitiousTime();
		this.operateTime = o.getOperateTime();
		this.operator = o.getOperator();
	}
	
	private String orderCode;//订单号
	private BigDecimal orderAmount;//订单金额
	private String orderType;//订单类型
	private String orderStatus;//订单状态
	private String productName;//产品名称
	private String mechanismFullName;//机构名称
	private Date fictitiousTime;//虚拟订单时间
	private Date operateTime;//操作时间
	private String operator;//操作人
	
	

}
