package com.guohuai.component.web.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PageInterestResp<T> extends BaseResp {

	protected long total;
	protected List<T> rows = new ArrayList<T>();
	protected BigDecimal raisedTotalVolume;
	protected BigDecimal hqlaTotalVolume;
	protected BigDecimal collectedTotalVolume;
	protected BigDecimal interestTotalAmount;
	protected BigDecimal repayTotalAmount;
	protected BigDecimal balanceCostTotalAmount;
	protected BigDecimal businessCostTotalAmount;

	public PageInterestResp(Page<T> page) {
		this(page.getTotalElements(), page.getContent(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
	}
}
