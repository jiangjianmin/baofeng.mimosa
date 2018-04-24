package com.guohuai.mmp.platform.tulip.log;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


public interface TulipLogDao extends JpaRepository<TulipLogEntity, String>, JpaSpecificationExecutor<TulipLogEntity> {
	
	@Query(value = "select * from T_MONEY_TULIP_LOG "
			+ "where sendedTimes < limitSendTimes and nextNotifyTime < sysdate() and oid > ?1 and errorCode != 0 order by oid limit 2000", nativeQuery = true)
	List<TulipLogEntity> getResendEntities(String lastOid);

}
