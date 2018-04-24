package com.guohuai.ams.investment.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvestmentLogDao
		extends JpaRepository<InvestmentLog, String>, JpaSpecificationExecutor<InvestmentLog> {

}
