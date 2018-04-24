package com.guohuai.mmp.platform.finance.resultdetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlatformFinanceCompareDataResultDetailDao extends JpaRepository<PlatformFinanceCompareDataResultDetailEntity, String>, JpaSpecificationExecutor<PlatformFinanceCompareDataResultDetailEntity>{


}
