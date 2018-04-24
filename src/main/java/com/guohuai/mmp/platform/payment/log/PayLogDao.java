package com.guohuai.mmp.platform.payment.log;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


public interface PayLogDao extends JpaRepository<PayLogEntity, String>, JpaSpecificationExecutor<PayLogEntity> {
	
	@Query(value = "select * from T_MONEY_PAY_LOG "
			+ "where sendedTimes < limitSendTimes and nextNotifyTime < sysdate() "
			+ " and errorCode != 0 limit 200", nativeQuery = true)
	List<PayLogEntity> getResendEntities();
	
	@Query(value = "from PayLogEntity where orderCode = ?1 and errorCode = 0 and handleType = 'applyCall' ")
	PayLogEntity getSuccessPayAplly(String orderCode);
	/*加入and errorCode = 0*/
	@Query(value = "select * from T_MONEY_PAY_LOG where orderCode = ?1 and handleType = 'notify' and errorCode = 0 limit 1", nativeQuery = true)
	PayLogEntity getPayNotify(String orderCode);

	PayLogEntity findByOrderCodeAndHandleType(String orderCode, String handleType);

}
