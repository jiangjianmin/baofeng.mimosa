package com.guohuai.mmp.investor.tradeorder.cycleProduct;

import com.guohuai.component.web.view.BaseReq;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class OrderContinueStatusReq extends BaseReq {

    private String orderCode;
    private int isContinue;

}
