package com.guohuai.mmp.platform.accment.log;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


public interface AccLogDao extends JpaRepository<AccLogEntity, String>, JpaSpecificationExecutor<AccLogEntity> {
	
	@Query(value = "select * from T_MONEY_ACC_LOG "
			+ "where sendedTimes < limitSendTimes and nextNotifyTime < sysdate() and oid > ?1 "
			+ " and errorCode != 0 order by oid limit 200", nativeQuery = true)
	List<AccLogEntity> getResendEntities(String lastOid);

}
