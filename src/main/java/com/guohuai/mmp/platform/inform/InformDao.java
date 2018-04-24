package com.guohuai.mmp.platform.inform;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InformDao extends JpaRepository<InformEntity, String>, JpaSpecificationExecutor<InformEntity> {

	
}
