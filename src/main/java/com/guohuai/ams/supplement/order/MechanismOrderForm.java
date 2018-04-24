package com.guohuai.ams.supplement.order;

import java.math.BigDecimal;
import java.sql.Timestamp;

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
public class MechanismOrderForm {
	
	private String mechanismOid;//所属机构
	private String productOid;//所属产品
	private BigDecimal orderAmount;//订单金额
	private String productName;//产品名称
	private Timestamp fictitiousTime;//虚拟订单时间
}
