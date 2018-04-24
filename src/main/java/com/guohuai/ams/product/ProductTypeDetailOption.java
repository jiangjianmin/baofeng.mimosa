package com.guohuai.ams.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTypeDetailOption {
	private String oid;
	private String type;
	private String title;
	private String url;
}
