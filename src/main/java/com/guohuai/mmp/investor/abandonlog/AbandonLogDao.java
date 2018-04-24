package com.guohuai.mmp.investor.abandonlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AbandonLogDao extends JpaRepository<AbandonLogEntity, String>, JpaSpecificationExecutor<AbandonLogEntity> {

	AbandonLogEntity findByRefundOrderCode(String refundOrderCode);
}
