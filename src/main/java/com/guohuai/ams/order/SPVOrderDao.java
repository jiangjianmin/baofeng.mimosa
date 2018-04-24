package com.guohuai.ams.order;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SPVOrderDao
		extends JpaRepository<SPVOrder, String>, JpaSpecificationExecutor<SPVOrder> {

	/**
	 * 查询满足条件的订单列表，计算资产池的实际市值
	 * @param pid
	 * 			资产池id
	 * @param baseDate
	 * 			基准日
	 * @author star
	 * @return
	 */
	@Query("from SPVOrder o where o.assetPool.oid = ?1 and o.orderType IN ('INVEST','REDEEM') and o.orderStatus = 'CONFIRM' and o.entryStatus = 'NO' and o.orderDate <= ?2")
	public List<SPVOrder> getListForMarketAdjust(String productOid, Date baseDate);
	
	/**
	 * 更新订单状态为已入账
	 * @param oid
	 */
	@Query(value = "update T_GAM_SPV_ORDER set entryStatus = 'YES' where oid = ?1", nativeQuery = true)
	@Modifying
	public void updateEntryStatus(String oid);
}
