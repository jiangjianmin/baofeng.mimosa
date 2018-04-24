package com.guohuai.ams.activityModel.ttzActivity;

import com.guohuai.component.web.view.BaseRep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TtzActivityInvestmentDataRep extends BaseRep{

    private String activityTotalInvest = "0.00";
    private String currentRatio = "0.00";
    private String myInvestAmount = "--";
    private String myExpectedIncome = "--";
    private String normalTnIncome = "--";

}
