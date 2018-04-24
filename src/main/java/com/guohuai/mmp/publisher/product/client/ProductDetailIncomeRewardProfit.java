package com.guohuai.mmp.publisher.product.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailIncomeRewardProfit {
	
	private String profit;//奖励收益率
	private String standard;
	private String level;//阶梯名称
	private String startDate;//起始天数	
	private String endDate;//截止天数

}
