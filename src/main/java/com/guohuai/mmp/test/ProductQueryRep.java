package com.guohuai.mmp.test;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProductQueryRep {
	
	String productOid;//产品OID
	
	String productName;//产品名称
	
	String productCode;//产品代码
	
	BigDecimal expAror;//产品利率起
	BigDecimal expArorSec;//产品利率止
	
	BigDecimal investMin;//起投金额
	
	int redeemConfirmDate;//赎回期
	
	String channelOid;//渠道OID
	
}
