package com.guohuai.mmp.platform.errorlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface PlatformErrorLogDao extends JpaRepository<PlatformErrorLogEntity, String>, JpaSpecificationExecutor<PlatformErrorLogEntity> {
	



}
