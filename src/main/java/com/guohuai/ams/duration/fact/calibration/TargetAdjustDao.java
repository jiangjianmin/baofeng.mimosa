package com.guohuai.ams.duration.fact.calibration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TargetAdjustDao
		extends JpaRepository<TargetAdjustEntity, String>, JpaSpecificationExecutor<TargetAdjustEntity> {

}
