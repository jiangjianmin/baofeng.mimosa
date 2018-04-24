package com.guohuai.mmp.publisher.cashflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublisherCashFlowDao extends JpaRepository<PublisherCashFlowEntity, String>, JpaSpecificationExecutor<PublisherCashFlowEntity> {

}
