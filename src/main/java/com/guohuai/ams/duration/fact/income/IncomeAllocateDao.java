package com.guohuai.ams.duration.fact.income;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IncomeAllocateDao extends JpaSpecificationExecutor<IncomeAllocate>, JpaRepository<IncomeAllocate, String> {

}
