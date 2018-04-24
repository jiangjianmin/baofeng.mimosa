package com.guohuai.mmp.investor.orderlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderLogDao extends JpaRepository<OrderLogEntity, String>, JpaSpecificationExecutor<OrderLogEntity> {
}
