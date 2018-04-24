package com.guohuai.ams.guess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GuessInvestItemDao  extends JpaRepository<GuessInvestItemEntity, String>, JpaSpecificationExecutor<GuessInvestItemEntity> {

}
