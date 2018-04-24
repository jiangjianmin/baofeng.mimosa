package com.guohuai.mmp.platform.redis;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RedisSyncDao extends JpaRepository<RedisSyncEntity, String>, JpaSpecificationExecutor<RedisSyncEntity> {


	

	

}
