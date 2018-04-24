package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.ams.product.Product;

import java.io.Serializable;
import java.math.BigDecimal;

public class PlusRedeemAfterReq implements Serializable{
    private static final long serialVersionUID = 509050651025516731L;

    private BigDecimal baseAmount;
    private String investOrderCode;
    private Product product;
    private BigDecimal difference;
    private BigDecimal fee;
    private InvestorTradeOrderEntity redeemOrderEntity;
    /** 预期收益 */
    private BigDecimal expectIncome;
    /**
     * 收益
     */
    private BigDecimal income;
    private String investorOid;

    public String getInvestorOid() {
        return investorOid;
    }

    public void setInvestorOid(String investorOid) {
        this.investorOid = investorOid;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpectIncome() {
        return expectIncome;
    }

    public void setExpectIncome(BigDecimal expectIncome) {
        this.expectIncome = expectIncome;
    }

    public String getInvestOrderCode() {
        return investOrderCode;
    }

    public void setInvestOrderCode(String investOrderCode) {
        this.investOrderCode = investOrderCode;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public InvestorTradeOrderEntity getRedeemOrderEntity() {
        return redeemOrderEntity;
    }

    public void setRedeemOrderEntity(InvestorTradeOrderEntity redeemOrderEntity) {
        this.redeemOrderEntity = redeemOrderEntity;
    }
}
