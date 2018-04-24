package com.guohuai.mmp.publisher.product.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentProductDetailProfit {
	
	private String profit;
	private String standard;

}
