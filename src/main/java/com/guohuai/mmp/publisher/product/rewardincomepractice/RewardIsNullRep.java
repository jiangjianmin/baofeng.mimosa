package com.guohuai.mmp.publisher.product.rewardincomepractice;

import java.math.BigDecimal;
import java.sql.Date;

import com.guohuai.ams.product.Product;
import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RewardIsNullRep extends BaseRep {
	private BigDecimal totalHoldVolume = BigDecimal.ZERO;
	private BigDecimal totalRewardIncome = BigDecimal.ZERO;
	private Product product;
	private Date tDate;
}
