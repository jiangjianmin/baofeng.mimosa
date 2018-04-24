package com.guohuai.mmp.publisher.investor.interest.fail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AllocateFailDao extends JpaRepository<AllocateFailEntity, String>, JpaSpecificationExecutor<AllocateFailEntity> {


}
