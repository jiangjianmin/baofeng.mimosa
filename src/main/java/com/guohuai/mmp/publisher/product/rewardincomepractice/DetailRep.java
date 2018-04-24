package com.guohuai.mmp.publisher.product.rewardincomepractice;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DetailRep extends BaseRep {
	BigDecimal totalHoldVolume = BigDecimal.ZERO; // 产品总规模
	BigDecimal buyVolume = BigDecimal.ZERO; // 用户申购总额

}
