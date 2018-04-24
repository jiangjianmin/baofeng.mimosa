package com.guohuai.mmp.platform.finance.result;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlatformFinanceCompareDataResultDao extends JpaRepository<PlatformFinanceCompareDataResultEntity, String>, JpaSpecificationExecutor<PlatformFinanceCompareDataResultEntity>{

	@Query(value = "DELETE FROM T_MONEY_CHECK_COMPAREDATA_RESULT WHERE checkOid = ?1 ", nativeQuery = true)
	@Modifying
	public int deleteByCheckOid(String checkOid);
	
	@Query(value = "UPDATE T_MONEY_CHECK_COMPAREDATA_RESULT "
			+ "SET dealStatus=?2 "
			+ "where oid=?1 ", nativeQuery = true)
	@Modifying
	public void updateDealStatusByOid(String oid, String dealStatus);
	
	@Query(value = "SELECT count(*) FROM T_MONEY_CHECK_COMPAREDATA_RESULT "
			+ "where checkOid=?1 AND dealStatus = 'dealing' ", nativeQuery = true)
	public Long countByCheckOid(String checkOid);
	
	@Query(value = "UPDATE T_MONEY_CHECK_COMPAREDATA_RESULT "
			+ "SET dealStatus = 'dealing' "
			+ "where (orderCode = ?1 or checkOrderCode = ?1)  and dealStatus = 'toDeal' ", nativeQuery = true)
	@Modifying
	public int updateDealStatusDealingByOrderCode(String orderCode);
	
	@Query(value = "UPDATE T_MONEY_CHECK_COMPAREDATA_RESULT "
			+ "SET dealStatus = 'dealt' "
			+ "where (orderCode = ?1 or checkOrderCode = ?1)  and dealStatus = 'dealing' ", nativeQuery = true)
	@Modifying
	public int updateDealStatusDealtByOrderCode(String orderCode);
	
	@Query(value = "SELECT a.orderCode, CASE WHEN a.orderType = 'invest' THEN '申购' WHEN a.orderType = 'normalRedeem' THEN '赎回' ELSE '' END orderType, a.orderAmount,"
			+ " CASE WHEN a.orderStatus = 'accepted' THEN '已受理' WHEN a.orderStatus = 'confirmed' THEN '份额已确认' WHEN a.orderStatus = 'done' THEN '交易成功' WHEN a.orderStatus = 'payFailed' THEN '支付失败' WHEN a.orderStatus = 'paySuccess' THEN '支付成功' ELSE '' END orderStatus,"
			+ " a.buzzDate,b.realName,a.checkOrderCode,"
			+ " CASE WHEN a.checkOrderType = 'invest' THEN '申购' WHEN a.checkOrderType = 'redeem' THEN '赎回' ELSE '' END checkOrderType, a.checkOrderAmount,"
			+ " CASE WHEN a.checkOrderStatus = 0 THEN '未处理' WHEN a.checkOrderStatus = 1 THEN '交易成功' WHEN a.checkOrderStatus = 2 THEN '交易失败' WHEN a.checkOrderStatus = 3 THEN '交易处理中' WHEN a.checkOrderStatus = 4 THEN '超时' WHEN a.checkOrderStatus = 5 THEN '撤销' ELSE '' END checkOrderStatus,"
			+ " c.realName as checkRealName,CASE WHEN a.checkStatus = 'moreThen' THEN '多帐' WHEN a.checkStatus = 'lessThen' THEN '少帐' WHEN a.checkStatus = 'equals' THEN '一致' WHEN a.checkStatus = 'exception' THEN '异常' ELSE '' END checkStatus"
			+ " FROM t_money_check_comparedata_result a LEFT JOIN t_money_investor_baseaccount b ON a.investorOid = b.memberId LEFT JOIN t_money_investor_baseaccount c ON a.checkInvestorOid = c.memberId"
			+ " WHERE ifnull(a.orderCode,'') = IF(?1='', ifnull(a.orderCode,''),?1)"
			+ " AND ifnull(a.orderAmount,0) >= IF(?2='', 0,?2)"
			+ " AND ifnull(a.orderAmount,0) <= IF(?7='', 999999999,?7)"
			+ " AND ifnull(a.orderStatus,'') = IF(?3='', ifnull(a.orderStatus,''),?3)"
			+ " AND ifnull(a.orderType,'') = IF(?4='', ifnull(a.orderType,''),?4)"
			+ " AND ifnull(a.checkStatus,'') = IF(?5='', ifnull(a.checkStatus,''),?5)"
			+ " AND ifnull(a.buzzDate,'') = IF(?6='', ifnull(a.buzzDate,''),?6)"
			+ " AND ifnull(a.checkOrderStatus,'') = IF(?8='', ifnull(a.checkOrderStatus,''),?8);", nativeQuery = true)
	public List<Object[]> findCompareDataResultDown(String orderCode,String orderAmount,String orderStatus,String orderType,String checkStatus,String checkDate,String orderAmountMax,String checkOrderStatus);

}
