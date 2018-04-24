package com.guohuai.mmp.publisher.baseaccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublisherBaseAccountEntityDao extends JpaRepository<PublisherBaseAccountEntity, String>,
		JpaSpecificationExecutor<PublisherBaseAccountEntity> {

}
