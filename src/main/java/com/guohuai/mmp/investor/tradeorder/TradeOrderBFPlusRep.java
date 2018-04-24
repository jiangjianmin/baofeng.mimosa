package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.component.web.view.BaseRep;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class TradeOrderBFPlusRep extends BaseRep implements Serializable {
    private static final long serialVersionUID = 6996655277556792957L;

    /**
     * 是否收取手续费
     */
    private boolean isFree;
    /**
     * 提前赎回手续费利率
     */
    private BigDecimal rate;
    /**
     * 转出最低本金金额
     */
    private BigDecimal limitAmount;
    /**
     * 最低手续费
     */
    private BigDecimal minimalFees;
    /**
     * 到账日
     */
    private Date toDate;
    /**
     * 年化利率
     */
    private BigDecimal apr;
    /**
     * 存续期
     */
    private Integer durationPeriodDays = 0;
    /**
     * 单笔投资最低份额
     */
    private BigDecimal investMin = new BigDecimal(0);
    /**
     * 收益计算基础
     */
    private String incomeCalcBasis;

    public String getIncomeCalcBasis() {
        return incomeCalcBasis;
    }

    public void setIncomeCalcBasis(String incomeCalcBasis) {
        this.incomeCalcBasis = incomeCalcBasis;
    }

    public BigDecimal getInvestMin() {
        return investMin;
    }

    public void setInvestMin(BigDecimal investMin) {
        this.investMin = investMin;
    }

    public Integer getDurationPeriodDays() {
        return durationPeriodDays;
    }

    public void setDurationPeriodDays(Integer durationPeriodDays) {
        this.durationPeriodDays = durationPeriodDays;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public BigDecimal getMinimalFees() {
        return minimalFees;
    }

    public void setMinimalFees(BigDecimal minimalFees) {
        this.minimalFees = minimalFees;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getApr() {
        return apr;
    }

    public void setApr(BigDecimal apr) {
        this.apr = apr;
    }
}
