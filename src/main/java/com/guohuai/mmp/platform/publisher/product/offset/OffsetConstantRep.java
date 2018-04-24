package com.guohuai.mmp.platform.publisher.product.offset;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OffsetConstantRep extends BaseRep {
	

	/**
	 * 净头寸
	 */
	private BigDecimal netPosition = BigDecimal.ZERO;

	
}
