package com.guohuai.ams.product.productChannel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProductChannelViewPage {

	private long total;

	private List<ProductChannelView> rows = new ArrayList<ProductChannelView>();

}
