package com.guohuai.ams.duration.capital.calc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ScheduleLogDao extends JpaRepository<ScheduleLog, String>, JpaSpecificationExecutor<ScheduleLog> {

	
}
