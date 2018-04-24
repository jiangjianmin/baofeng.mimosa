package com.guohuai.ams.duration.capital.calc.bank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BankCalcDao extends JpaRepository<BankCalc, String>, JpaSpecificationExecutor<BankCalc> {

	
}
