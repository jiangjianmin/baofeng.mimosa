package com.guohuai.ams.cashtool.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CashToolLogDao
		extends JpaRepository<CashToolLog, String>, JpaSpecificationExecutor<CashToolLog> {

}
