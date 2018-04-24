package com.guohuai.mmp.platform.publisher.product.offset;

import java.math.BigDecimal;

import com.guohuai.component.web.view.RowsRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductOffsetMoneyRep extends RowsRep<ProductOffsetMoneyRep> {
	String productOid;
	
	String productCode;
	String productName;
	
	
	/**
	 * 申购金额
	 */
	private BigDecimal investAmount;

	/**
	 * 赎回金额
	 */
	private BigDecimal redeemAmount;



	/**
	 * 净头寸
	 */
	private BigDecimal netPosition;
	
	
}
