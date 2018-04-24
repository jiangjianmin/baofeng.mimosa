package com.guohuai.mmp.platform.reserved.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReservedOrderDao extends JpaRepository<ReservedOrderEntity, String>, JpaSpecificationExecutor<ReservedOrderEntity> {

	
	

	

}
