package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.ams.product.ProductPojo;
import com.guohuai.component.web.view.BaseRep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TtzActivityProductListRep extends BaseRep{

    private List<ProductPojo> rows = new ArrayList<>();

}
