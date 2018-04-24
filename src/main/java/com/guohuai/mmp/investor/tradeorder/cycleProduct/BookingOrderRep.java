package com.guohuai.mmp.investor.tradeorder.cycleProduct;

import com.guohuai.component.web.view.BaseRep;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@lombok.Data
@NoArgsConstructor
public class BookingOrderRep extends BaseRep {

    private List<Map<String, Object>> rows;
    private int total;
    private int page;
    private int size;

}
