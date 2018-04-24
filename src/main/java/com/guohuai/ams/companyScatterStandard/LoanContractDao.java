package com.guohuai.ams.companyScatterStandard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LoanContractDao extends JpaRepository<LoanContract, String>, JpaSpecificationExecutor<LoanContract> {

    @Query(value = "select * from T_GAM_LOAN_CONTRACT t1 WHERE t1.code = ?1 ", nativeQuery = true)
    LoanContract getLoanContractByCode(String code);

}
