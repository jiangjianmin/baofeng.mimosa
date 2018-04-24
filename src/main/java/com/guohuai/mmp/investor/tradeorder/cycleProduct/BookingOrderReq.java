package com.guohuai.mmp.investor.tradeorder.cycleProduct;

import com.guohuai.component.web.view.BaseReq;
import lombok.NoArgsConstructor;

import java.sql.Date;

@lombok.Data
@NoArgsConstructor
public class BookingOrderReq extends BaseReq {

    private String productName;
    private String phone;
    private String orderTimeBegin;
    private String orderTimeEnd;
    private String orderStatus;
    private String payType;
    private int page = 1;
    private int size = 10;

}
