package com.guohuai.ams.product.cycleProduct;


import com.guohuai.ams.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ContinueInvestTransferEvent {

    private int page;

    BigDecimal availableAmount;

    private Product product;

    private List<String> investorOids;

    private ConcurrentLinkedDeque<BigDecimal> sumAmount;

    private Map<String, String> tmpMap = new HashMap<>();

    public ContinueInvestTransferEvent(int page, BigDecimal availableAmount, Product product, List<String> investorOids, ConcurrentLinkedDeque<BigDecimal> sumAmount) {
        this.page = page;
        this.availableAmount = availableAmount;
        this.product = product;
        this.investorOids = investorOids;
        this.sumAmount = sumAmount;
    }
}
