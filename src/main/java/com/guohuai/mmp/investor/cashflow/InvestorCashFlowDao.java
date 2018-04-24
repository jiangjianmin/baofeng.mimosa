package com.guohuai.mmp.investor.cashflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvestorCashFlowDao extends JpaRepository<InvestorCashFlowEntity, String>, JpaSpecificationExecutor<InvestorCashFlowEntity> {

}
