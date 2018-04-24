package com.guohuai.ams.productPackage.loan;

import com.guohuai.component.web.view.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CompanyLoanProductReq extends BaseReq {

    /**
     * 标的编号
     */
    private String productCode;

    /**
     * 标的名称
     */
    private String productName;

    /**
     * 借款期限（单位月）
     */
    private int loanPeriod;

    /**
     * 借款金额（单位元#.00）
     */
    private BigDecimal loanAmount;

    /**
     * 借款利率（百分比折合小数）
     */
    private BigDecimal loanRatio;

    /**
     * 借款用途
     */
    private String loanUsage;

    /**
     * 还款方式
     */
    private String refundMode;

    /**
     * 企业编号
     */
    private String orgCode;

    /**
     * 企业名称
     */
    private String orgName;

    /**
     * 企业法人
     */
    private String orgCorporationName;

    /**
     * 企业注册资本
     */
    private String registeredCapital;

    /**
     * 企业成立时间
     */
    private String setupDate;

    /**
     * 借款机构地址
     */
    private String orgAddress;

}
