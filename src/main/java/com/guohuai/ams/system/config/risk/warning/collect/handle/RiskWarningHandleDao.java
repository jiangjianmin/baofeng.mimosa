package com.guohuai.ams.system.config.risk.warning.collect.handle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RiskWarningHandleDao
		extends JpaRepository<RiskWarningHandle, String>, JpaSpecificationExecutor<RiskWarningHandle> {

}
