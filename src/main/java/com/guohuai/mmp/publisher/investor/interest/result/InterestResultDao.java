package com.guohuai.mmp.publisher.investor.interest.result;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterestResultDao extends JpaRepository<InterestResultEntity, String>, JpaSpecificationExecutor<InterestResultEntity> {


}
