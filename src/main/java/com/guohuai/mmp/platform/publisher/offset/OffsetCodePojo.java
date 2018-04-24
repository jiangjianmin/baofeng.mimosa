package com.guohuai.mmp.platform.publisher.offset;

import com.guohuai.ams.product.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffsetCodePojo {
	private String startTime;
	private String endTime;
	private Product product;
	
	
}
