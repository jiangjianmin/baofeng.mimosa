package com.guohuai.mmp.publisher.holdapart.closedetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CloseDetailsDao extends JpaRepository<CloseDetailsEntity, String>, JpaSpecificationExecutor<CloseDetailsEntity> {


}
