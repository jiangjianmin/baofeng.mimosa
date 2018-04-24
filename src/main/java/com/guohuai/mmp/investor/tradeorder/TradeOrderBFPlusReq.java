package com.guohuai.mmp.investor.tradeorder;

import java.io.Serializable;

public class TradeOrderBFPlusReq implements Serializable {
    private static final long serialVersionUID = -7679495679866280996L;

    private String orderCode;

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
}
