package com.guohuai.mmp.platform.publisher.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublisherOrderDao extends JpaRepository<PublisherOrderEntity, String>, JpaSpecificationExecutor<PublisherOrderEntity> {

	PublisherOrderEntity findByOrderCode(String orderCode);
	

	

}
