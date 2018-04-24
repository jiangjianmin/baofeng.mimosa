package com.guohuai.mmp.platform.accountingnotify;

import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AccountingNotifyDao extends JpaRepository<AccountingNotifyEntity, String>, JpaSpecificationExecutor<AccountingNotifyEntity> {

	@Query(nativeQuery = true, value = "SELECT notifyType,SUM(costFee) FROM T_MONEY_PLATFORM_ACCOUNTINGNOTIFY WHERE channelOid= ?1 and (notifyType = 'payPlatformFee' OR notifyType = 'payPlatformCouFee') GROUP BY notifyType")
	List<Object[]> totalActualOfPayment(String channelOid);

	@Query(value = "FROM AccountingNotifyEntity a WHERE a.productOid = ?1 and a.busDate=?2 AND (a.notifyType = 'offsetPay' OR a.notifyType = 'offsetPayCouFee' OR a.notifyType= 'offsetCollect') ")
	Page<AccountingNotifyEntity> findOffsetFee(String productOid, Date busDay, Pageable pageable);

	@Query(value = "FROM AccountingNotifyEntity a WHERE a.productOid = ?1 AND (a.notifyType = 'payPlatformFee' OR a.notifyType = 'payPlatformCouFee') ")
	Page<AccountingNotifyEntity> getFeeListByOid(String productOid, Pageable pageable);
}
