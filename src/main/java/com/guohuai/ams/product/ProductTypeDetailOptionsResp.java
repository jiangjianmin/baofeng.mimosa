package com.guohuai.ams.product;

import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseResp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper=false)
public class ProductTypeDetailOptionsResp extends BaseResp{
	private List<ProductTypeDetailOption> productTypeDetailOptions = new ArrayList<>();
}
