package com.guohuai.mmp.lx.fee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FeeDao extends JpaRepository<FeeEntity, String>, JpaSpecificationExecutor<FeeEntity> {
}
