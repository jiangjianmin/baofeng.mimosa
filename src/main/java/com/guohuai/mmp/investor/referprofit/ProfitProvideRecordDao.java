package com.guohuai.mmp.investor.referprofit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfitProvideRecordDao extends JpaRepository<ProfitProvideRecordEntity, String>,JpaSpecificationExecutor<ProfitProvideRecordEntity> {

}
