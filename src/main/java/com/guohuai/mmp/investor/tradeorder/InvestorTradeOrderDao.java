package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import com.guohuai.mmp.investor.tradeorder.cycleProduct.BookingOrderRep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.product.Product;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.platform.investor.offset.InvestorOffsetEntity;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.TradeOrderHoldDaysDetail;

public interface InvestorTradeOrderDao extends JpaRepository<InvestorTradeOrderEntity, String>, JpaSpecificationExecutor<InvestorTradeOrderEntity> {
	
	/**
	 * 更新投资人清算状态
	 * @param offset 投资人轧差
	 * @param investorClearStatus 清算状态
	 * @return
	 */
	@Query("update InvestorTradeOrderEntity set investorClearStatus = ?2 "
			+ " where investorOffset = ?1 and investorClearStatus = 'toClear' and orderStatus in ('accepted', 'done') ")
	@Modifying
	public int updateInvestorClearStatus(InvestorOffsetEntity offset,String investorClearStatus);
	
	/**
	 * 更新投资人结算状态
	 */
	@Query("update InvestorTradeOrderEntity set investorCloseStatus = ?2 where investorOffset = ?1  and orderStatus in ('accepted', 'done') "
			+ " and investorCloseStatus in ('toClose','closeSubmitFailed','closePayFailed') ")
	@Modifying
	public int updateInvestorCloseStatus(InvestorOffsetEntity offset,String investorCloseStatus);
	
	@Query("update InvestorTradeOrderEntity set investorCloseStatus = ?2 where investorOffset = ?1  and orderStatus in ('accepted', 'done') "
			+ " and investorCloseStatus = 'toClose' ")
	@Modifying
	public int updateInvestorCloseStatusDirectly(InvestorOffsetEntity offset,String investorCloseStatus);
	
	
	/**
	 * 更新超级用户投资人结算状态
	 */
	@Query("update InvestorTradeOrderEntity set investorCloseStatus = ?3 "
			+ " where investorOffset = ?1 and  investorOid = ?2 and investorClearStatus = 'cleared' and orderStatus in ('accepted', 'done') "
			+ " and investorCloseStatus in ('toClose','closeSubmitFailed','closePayFailed') ")
	@Modifying
	public int updatePlatformInvestorCloseStatus(InvestorOffsetEntity offset, String investorOid, String investorCloseStatus);
	
	public InvestorTradeOrderEntity findByOrderCode(String orderCode);
	
//	@Query(value="select * from T_MONEY_INVESTOR_TRADEORDER where oid > ?3 and orderStatus in ('accepted', 'done') "
//			+ "and investorCloseStatus in ('toClose','closeSubmitFailed','closePayFailed') "
//			+ "and investorClearStatus = 'cleared' and investorOffsetOid = ?1 and investorOid != ?2 order by oid limit 300", nativeQuery = true)
//	public List<InvestorTradeOrderEntity> findToCloseOrders(String investorOffsetOid,  String investorOid, String oid);
//	
//	@Query(value="select * from T_MONEY_INVESTOR_TRADEORDER where orderStatus in ('accepted', 'done') "
//			+ " and investorCloseStatus in ('toClose','closeSubmitFailed','closePayFailed') "
//			+ " and investorClearStatus = 'cleared' and investorOffsetOid = ?1 and investorOid = ?2 ", nativeQuery = true)
//	public List<InvestorTradeOrderEntity> findToClosePlatformOrders(String investorOffsetOid, String investorOid);
	
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER where oid > ?2 and orderStatus in ('accepted', 'confirmed') "
			+ " and publisherCloseStatus in ('toClose') and publisherOffsetOid = ?1 order by oid limit 300", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findToCloseOrders(String publisherOffsetOid, String oid);
	
	public List<InvestorTradeOrderEntity> findByInvestorOffsetAndOrderType(InvestorOffsetEntity offset,String orderType);

	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER entity where publisherOffsetOid = ?1 and oid > ?2 "
			+ "  and publisherClearStatus = 'cleared' and publisherConfirmStatus in ('confirmFailed', 'toConfirm')  order by oid limit 2000", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findByOffsetOidAndOid(String offsetOid, String lastOid);
	
	
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER entity where publisherOffsetOid = ?1 and oid > ?2 "
			+ "  and publisherClearStatus = 'cleared' and publisherConfirmStatus='confirmed' order by oid limit 2000", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findConfirmedByOffsetOidAndOid(String offsetOid, String lastOid);
	/**
	 * 查询订单用于生成PDF协议 HTML
	 */
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER "
			+ " where productOid = ?1 and oid > ?2 and publisherConfirmStatus = 'confirmed' "
			+ " and orderType = 'invest' and contractStatus in ('htmlFail', 'toHtml') order by oid limit 2000", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findByProductOid4Contract(String productOid, String lastOid);
	
	/**
	 * 查询订单用于生成PDF协议 PDF
	 */
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER "
			+ " where productOid = ?1 and oid > ?2 and publisherConfirmStatus = 'confirmed' "
			+ " and orderType = 'invest' and contractStatus in ('htmlOK') order by oid limit 2000", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findByProductOid4PDF(String productOid, String lastOid);
	

	@Query(value = "update T_MONEY_INVESTOR_TRADEORDER set publisherClearStatus = ?2 "
			+ "where publisherOffsetOid = ?1 and publisherClearStatus = 'toClear' and orderStatus = 'accepted' ", nativeQuery = true)
	@Modifying
	public int updatePublisherClearStatus(String offsetOid, String clearStatus);
	
	/**
	 * 份额确认 
	 */
	@Query(value = "update InvestorTradeOrderEntity set publisherConfirmStatus = 'confirmed', orderStatus = 'confirmed', completeTime = sysdate()  "
			+ " where oid = ?1 and publisherConfirmStatus in ('toConfirm', 'confirmFailed') and publisherClearStatus = 'cleared' ")
	@Modifying
	public int update4Confirm(String oid);
	
	/**
	 * 结算 
	 */
	@Query(value = "update T_MONEY_INVESTOR_TRADEORDER set publisherCloseStatus = ?2  "
			+ " where publisherOffsetOid = ?1  and publisherClearStatus = 'cleared' "
			+ " and publisherCloseStatus in ('closeSubmitFailed', 'closePayFailed', 'toClose')  ", nativeQuery = true)
	@Modifying
	public int updateCloseStatus4Close(String pOffsetOid, String closeStatus);
	
	
	@Query(value = "update T_MONEY_INVESTOR_TRADEORDER set publisherCloseStatus = ?2 "
		+ "where publisherOffsetOid = ?1 and publisherClearStatus = 'cleared' and publisherCloseStatus != 'closed' ", nativeQuery = true)
	@Modifying
	public int updateCloseStatus4CloseBack(String pOffsetOid, String closeStatus);
	
	
	@Query(value = "update T_MONEY_INVESTOR_TRADEORDER set orderStatus ='refused', "
			+ " publisherClearStatus = null, publisherConfirmStatus = null, publisherCloseStatus = null, investorClearStatus = null, investorCloseStatus = null "
			+ " where orderCode = ?1 and orderType in ('normalRedeem','incrementRedeem') and publisherClearStatus = 'toClear' and orderStatus = 'accepted' ", nativeQuery = true)
	@Modifying
	public int refuseOrder(String orderCode);
	
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER "
			+ "where productOid = ?1 and oid > ?2 and orderType in ('normalRedeem','incrementRedeem') and orderStatus = 'accepted'"
			+ " and publisherClearStatus = 'toClear' order by oid limit 1000", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findByProduct(String productOid, String lastOid);
	
	/**
	 * totalRewardIncome跟couponAmount
	 * @param productOid
	 * @return
	 */
	@Query(value = "select sum(truncate(IFNULL(totalRewardIncome,0),2)+IFNULL(couponAmount,0)) from T_MONEY_INVESTOR_TRADEORDER "
			+ "where productOid = ?1 and orderType in ('invest','noPayInvest') and orderStatus = 'confirmed'", nativeQuery = true)
	public BigDecimal findSumTotalRewardIncome(String productOid);
	
	@Query(value = "select sum(truncate(totalBaseIncome,2)) from T_MONEY_INVESTOR_TRADEORDER "
			+ "where productOid = ?1 and orderType in ('invest','noPayInvest') and orderStatus = 'confirmed'", nativeQuery = true)
	public BigDecimal findSumTotalBaseIncome(String productOid);
	
	@Query(value = "update T_MONEY_INVESTOR_TRADEORDER set orderStatus = 'abandoning', holdStatus = 'abandoned' "
			+ " where orderCode = ?1 and orderType in ('invest') and publisherClearStatus = 'toClear' and orderStatus = 'accepted'", nativeQuery = true)
	@Modifying
	public int abandonT0Order(String orderCode);
	
	@Query(value = "update T_MONEY_INVESTOR_TRADEORDER set orderStatus = 'abandoning', holdStatus = 'abandoned' "
			+ " where orderCode = ?1 and orderType in ('invest', 'noPayInvest') and orderStatus = 'confirmed'", nativeQuery = true)
	@Modifying
	public int abandonTnOrder(String orderCode);
	
	@Query(value = "update T_MONEY_INVESTOR_TRADEORDER set orderStatus = 'abandoning'  "
			+ " where orderCode = ?1 and orderType = 'invest' and orderStatus = 'paySuccess' ", nativeQuery = true)
	@Modifying
	public int refundOrder(String orderCode);
	
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER where oid > ?1 and orderStatus in ('toRefund', 'refundFailed') order by oid limit 300", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findPage4Refund(String lastOid);
	
	/** 统计发行人昨日各产品投资金额 */
	@Query(value = "SELECT A.publisherOid,A.productOid,SUM(A.orderAmount) investAmount  "
				+" FROM T_MONEY_INVESTOR_TRADEORDER A "
				+" WHERE A.orderType = 'invest'  "
				+" AND A.orderStatus IN ('paySuccess','accepted','confirmed', 'done') "
				+" AND A.orderTime BETWEEN ?1 AND ?2  "
				+" GROUP BY A.publisherOid,A.productOid ", nativeQuery = true)
	public List<Object[]> countPublishersYesterdayInvestAmount(Timestamp startTime, Timestamp endTime);
	
	/** 统计发行人截止昨日各产品累计投资金额 */
	@Query(value = "SELECT A.publisherOid,A.productOid,SUM(A.orderAmount) investAmount  "
				+" FROM T_MONEY_INVESTOR_TRADEORDER A "
				+" WHERE A.orderType = 'invest'  "
				+" AND A.orderStatus IN ('paySuccess','accepted','confirmed', 'done') "
				+" AND A.orderTime <= ?1  "
				+" GROUP BY A.publisherOid,A.productOid ", nativeQuery = true)
	public List<Object[]> countPublishersTotalInvestAmount(Timestamp endTime);
	
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER where oid > ?1 and orderStatus = 'toPay' and (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(createTime))/60 > 15 order by oid limit 2000", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findPage4RecoveryHold(String lastOid);
	
	
	/**统计各渠道昨日投资额*/
	@Query(value = " SELECT A.channelOid,A.orderType,SUM(A.orderAmount) amount "
				+" FROM T_MONEY_INVESTOR_TRADEORDER A  "
				+" WHERE A.channelOid is not null and A.orderTime BETWEEN ?1 AND ?2 "
				+ " AND A.orderType='invest' "
				+ " AND A.orderStatus IN ('paySuccess','accepted','confirmed','done') "
				+" GROUP BY A.channelOid,A.orderType ", nativeQuery = true)
	public List<Object[]> statInvestAmountByChannel(Timestamp startTime, Timestamp endTime);
	
	/**统计各渠道截止到昨日累计投资额*/
	@Query(value = " SELECT A.channelOid,A.orderType,SUM(A.orderAmount) amount "
				+" FROM T_MONEY_INVESTOR_TRADEORDER A  "
				+" WHERE A.channelOid is not null and A.orderTime <=?1"
				+" AND A.orderType='invest' "
				+" AND A.orderStatus IN ('paySuccess','accepted','confirmed','done') "
				+" GROUP BY A.channelOid,A.orderType ", nativeQuery = true)
	public List<Object[]> statInvestTotalAmountByChannel(Timestamp endTime);

	public InvestorTradeOrderEntity findByCoupons(String coupons);
	
	/**
	 * 份额确认（分仓）
	 */
	@Modifying
	@Query(value = "update InvestorTradeOrderEntity set holdStatus = 'holding', redeemStatus = ?2, "
			+ "accrualStatus = ?3, completeTime = sysdate() where oid = ?1")
	public int update4Confirm(String orderOid, String redeemStatus, String accrualStatus);
	
	/**
	 * 查询可平仓的分仓
	 */
	@Query(value = "from InvestorTradeOrderEntity "
			+ " where investorBaseAccount = ?1 and product = ?2 and holdVolume > 0"
			+ " and holdStatus in ('holding','partHolding') "
			+ " and redeemStatus = 'yes' order by createTime ")
	public List<InvestorTradeOrderEntity> findApart(InvestorBaseAccountEntity investorBaseAccount, Product product);
	
	/**
	 *计息快照
	 */
	@Modifying
	@Query(value = "insert into T_MONEY_PUBLISHER_INVESTOR_HOLD_SNAPSHOT (oid, orderOid, investorOid, productOid, holdOid, holdDays, redeemStatus, "
			+ " snapshotVolume, snapShotDate, updateTime, createTime) "
			+ " select REPLACE(uuid(), '-', ''), oid, investorOid, productOid, holdOid, TIMESTAMPDIFF(DAY, beginAccuralDate, ?2) + 1, redeemStatus, "
			+ " holdVolume, ?2, sysdate(), sysdate() from T_MONEY_INVESTOR_TRADEORDER "
			+ " where productOid = ?1 AND beginAccuralDate <= ?2 "
			+ " and holdStatus in ('holding','partHolding') AND holdVolume > 0", nativeQuery = true)
	public int snapshotVolume(String productOid, Date incomeDate);
	
	/**
	 *计息快照 (体验金利息)
	 */
	@Modifying
	@Query(value = "insert into T_MONEY_PUBLISHER_INVESTOR_HOLD_SNAPSHOT (oid, orderOid, investorOid, productOid, holdOid, holdDays, redeemStatus, "
			+ " snapshotVolume, snapShotDate, updateTime, createTime) "
			+ " select REPLACE(uuid(), '-', ''), oid, investorOid, productOid, holdOid, TIMESTAMPDIFF(DAY, beginAccuralDate, ?2) + 1, redeemStatus, "
			+ " TRUNCATE(IF(corpusAccrualEndDate <= ?2, IF(holdVolume > orderVolume, holdVolume - orderVolume, holdVolume), holdVolume),2), ?2, sysdate(), sysdate() from T_MONEY_INVESTOR_TRADEORDER "
			+ " where productOid = ?1 AND beginAccuralDate <= ?2 "
			+ " and holdStatus in ('holding','partHolding')  and  ?2 < beginRedeemDate", nativeQuery = true)
	public int snapshotTasteCouponVolume(String productOid, Date incomeDate);
	
	
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER where beginRedeemDate <= ?1 "
			+ "and redeemStatus = 'no' and (couponType != 'tasteCoupon' or couponType is null) and oid > ?2 and holdStatus IN ('holding', 'partHolding') order by oid limit 1000 ", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findByBeforerBeginRedeemDateInclusive(Date today, String oid);
	
	/**
	 * 根据可计算状态，持仓状态，开始起息日查找分仓
	 */
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER where beginAccuralDate <= ?1 and accrualStatus = 'no' and oid > ?2 and holdStatus IN ('holding', 'partHolding') order by oid limit 1000 ", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findByBeforeBeginAccuralDateInclusive(Date date, String oid);
	
	/**
	 * 更新可赎回状态
	 */
	@Query(value = "update InvestorTradeOrderEntity set redeemStatus = 'yes' where oid = ?1")
	@Modifying
	public int unlockRedeem(String orderOid);
	/**
	 * 更新可计息状态
	 */
	@Query(value = "update InvestorTradeOrderEntity set accrualStatus = 'yes' where oid = ?1")
	@Modifying
	public int unlockAccrual(String orderOid);
	
	/**
	 * 更新可赎回状态
	 */
	@Query(value = "update InvestorTradeOrderEntity set redeemStatus = 'yes' where publisherHold = ?1")
	@Modifying
	public int unlockRedeemByHold(PublisherHoldEntity hold);
	
	/**
	 * 查询可计息分仓
	 */
	@Query(value = "from InvestorTradeOrderEntity "
			+ " where publisherHold = ?1 and beginAccuralDate <= ?2 and holdStatus in ('holding','partHolding')")	
	public List<InvestorTradeOrderEntity> findInterestableApart(PublisherHoldEntity hold, Date curDate);
	/**
	 * 查询使用体验金且状态为持有中的订单
	 * @return
	 */
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER where holdStatus = 'holding' and holdOid = ?1 and confirmDate is not null and  couponType = 'tasteCoupon'  ", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findtTasteCouponHolding(String holdOid);
	
	
	@Query(value = "select count(*) from T_MONEY_INVESTOR_TRADEORDER where productOid = ?1 and holdStatus = 'toConfirm' ", nativeQuery = true)
	public int getToConfirmCountByProduct(String productOid);
	
	@Query(value = "update InvestorTradeOrderEntity set holdVolume = holdVolume + incomeAmount, incomeAmount = 0 where product = ?1  ")
	@Modifying
	public int changeIncomeIntoHoldVolume(Product product);
	
	@Query(value = "update InvestorTradeOrderEntity set redeemStatus = 'yes' where product = ?1")
	@Modifying
	public void unlockRedeem4Cash(Product product);
	
	
	/**
	 * 分配收益
	 */
	@Modifying
	@Query(value = "update InvestorTradeOrderEntity set holdVolume = holdVolume + ?2, "
			+ " value = holdVolume * ?4,"
			+ " totalIncome = totalIncome + ?3, "
			+ " yesterdayIncome = ?3, "
			+ " totalBaseIncome = totalBaseIncome + ?6, "
			+ " yesterdayBaseIncome = ?6, "
			+ " totalRewardIncome = totalRewardIncome + ?7, "
			+ " yesterdayRewardIncome = ?7, confirmDate = ?5 where oid = ?1")	
	public int updateHoldApart4Interest(String apartOid, BigDecimal incomeVolume, BigDecimal incomeAmount, 
			BigDecimal netUnitAmount, Date incomeDate, BigDecimal baseAmount, BigDecimal rewardAmount);
	
	@Modifying
//	@Query(value = "update InvestorTradeOrderEntity set incomeAmount = incomeAmount + ?2, value = value + ?2,"
//			+ " confirmDate = ?3 where oid = ?1")
	@Query(value = "update InvestorTradeOrderEntity set holdVolume = holdVolume + ?2, "
			+ " value = value + ?2,"
			+ " totalIncome = totalIncome + ?2, "
			+ " yesterdayIncome = ?2, "
			+ " totalBaseIncome = totalBaseIncome + ?2, "
			+ " yesterdayBaseIncome = ?2, "
			+ " confirmDate = ?3 where oid = ?1")
	public int updateHoldApart4InterestTn(String orderOid, BigDecimal incomeAmount, Date incomeDate);

	public List<InvestorTradeOrderEntity> findByPublisherOffset(PublisherOffsetEntity offset);
	
	@Query(value="SELECT a.orderCode,a.orderStatus,a.orderType,a.orderAmount-ifnull(a.couponAmount,0),b.memberId "
			+ "FROM T_MONEY_INVESTOR_TRADEORDER a "
			+ "INNER JOIN T_MONEY_INVESTOR_BASEACCOUNT b ON a.investorOid=b.oid "
			+ "WHERE a.orderTime BETWEEN ?1 AND ?2 "
			+ "AND a.orderCode > ?3 "
			+ "AND ( a.couponType != 'tasteCoupon' OR a.couponType IS NULL) AND a.orderType IN('invest','normalRedeem','incrementRedeem') "
			+ "AND a.orderStatus IN('payFailed','paySuccess','accepted','confirmed','done') "
			+ "ORDER BY a.orderCode LIMIT 2000 ",nativeQuery = true)
	public List<Object[]> findInvestorOrderByOrderTime(String beginTime,String endTime,String orderCode);
	@Modifying
	@Query(value = "update InvestorTradeOrderEntity set holdVolume = holdVolume - ?2,"
			+ "  redeemStatus = 'yes' where oid = ?1 and holdVolume >= ?2")
	public int flatExpGoldVolume(String oid, BigDecimal orderVolume);
	
	
	/**
	 * 查询待平仓体验金
	 */
	@Query(value = "select * from T_MONEY_INVESTOR_TRADEORDER where "
			+ " beginRedeemDate <= ?2 and couponType = 'tasteCoupon' and redeemStatus = 'no' and oid > ?1 order by oid limit 2000", nativeQuery = true)
	public List<InvestorTradeOrderEntity> queryFlatExpGold(String lastOid, Date baseDate);
	
	
	@Query(value = "update InvestorTradeOrderEntity set orderStatus = 'abandoned' where orderCode = ?1 and orderStatus = 'abandoning' ")
	@Modifying
	public int updateOrderStatus4Abandon(String orderCode);
	
	@Query(value = "update InvestorTradeOrderEntity set orderStatus = 'refunded' where orderCode = ?1 and orderStatus = 'refunding' ")
	@Modifying
	public int updateOrderStatus4Refund(String orderCode);

	@Query(value = "from InvestorTradeOrderEntity where publisherHold = ?1 and orderStatus='confirmed'")
	public List<InvestorTradeOrderEntity> findByPublisherHold(PublisherHoldEntity hold);
//	@Query(value = "update InvestorTradeOrderEntity set orderStatus = 'abandoned' where orderCode = ?1 and orderStatus != 'abandoned'")
//	@Modifying
//	public int updateByOrderCodeAndStatus(String originalRedeemOrderCode);
	@Query(value = "update InvestorTradeOrderEntity set orderStatus = 'payFailed' where orderCode = ?1 and orderStatus != 'payFailed'")
	@Modifying
	public int updateByOrderCodeAndStatus(String originalRedeemOrderCode);
	
	@Query(value = "select b.* from t_money_investor_baseaccount a,t_money_investor_tradeorder b "
			+ "where a.oid = b.investorOid and a.userOid = ?1 and b.orderType = 'specialRedeem' and b.orderStatus!='done' and b.orderStatus!='refused' and b.orderStatus!='abandoned'", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findSpecialRedeemOrder(String userOid); 
	

//	@Query(value = "SELECT count(1) FROM t_money_investor_tradeorder WHERE investorOid=?1 AND orderStatus NOT IN ('refused','payFailed','payExpired','confirmed','done','refunded','abandoned')", nativeQuery = true)
	@Query(value="SELECT count(1) FROM t_money_investor_tradeorder "
			+ "    WHERE investorOid=?1 AND "
			+ "    CASE WHEN orderType IN ('invest','noPayInvest','writeOff') "
			+ "			  THEN orderStatus NOT IN ('refused','payFailed','payExpired','confirmed','done','refunded','abandoned') "
			+ "         WHEN orderType IN ('noPayRedeem','normalRedeem','specialRedeem','cash','incrementRedeem') "
			+ "           THEN orderStatus NOT IN ('refused','payFailed','payExpired','done','refunded','abandoned')"
			+ "    END", nativeQuery = true)
	public int findNotDoneQty(String investorOid);

	@Query(value = "select count(*) "
			+ "FROM  t_money_investor_tradeorder a   LEFT JOIN t_money_investor_baseaccount b  ON a.investorOid = b.oid  LEFT JOIN t_gam_product c  ON c.oid=a.productOid  LEFT JOIN t_money_platform_channel ch	on ch.oid=a.channelOid  "
			+ " LEFT JOIN t_money_publisher_baseaccount ba on ba.oid=a.publisherOid LEFT JOIN T_MONEY_CORPORATE co on co.oid=ba.corperateOid "
			+ " left JOIN T_MONEY_PLATFORM_PUBLISHER_OFFSET pu ON a.publisherOffsetOid=pu.oid "
			+ "where ((a.`orderType`='invest' and  a.orderStatus='confirmed') or (a.`orderType` in ('normalRedeem','incrementRedeem') and  a.orderStatus='done')) "
			+ " and ( a.orderType =?1  or ?1 =''||' ')"
			+ " and ( a.orderAmount >=?2  or ?2 =''||' ')"
			+ " and ( a.orderAmount <=?3  or ?3 =''||' ')"
			+ " and ( b.phoneNum =?4  or ?4 =''||' ')"
			+ " and ( b.realName =?5  or ?5 =''||' ')"
			+ " and ( b.idNum =?6  or ?6 =''||' ')"
			+ " and ( a.orderTime >=?7  or ?7 =''||' ')"
			+ " and ( a.orderTime <=?8  or ?8 =''||' ')"
			+ " and ( pu.offsetCode =?9  or ?9 =''||' ')"
			+ "", nativeQuery = true)
	public int findInvestTotal(String orderType,String bigDecimal,String bigDecimal2,String phoneNum,String realName,String idNum,String beginTme,String endTime,String offsetCode); 
	@Query(value = "select * from (select  b.phoneNum  , b.realName ,co.account , c.name,  ch.channelName,  a.investorOffsetOid ,pu.offsetCode,  a.checkOid,  a.holdOid ,  a.orderCode ,  a.orderType,"
			+ "a.orderAmount ,  a.orderVolume ,  a.payAmount,  a.couponAmount,  a.coupons ,  a.couponType,  a.payStatus ,  a.acceptStatus,  a.refundStatus ,  a.orderStatus ,"
			+ "a.checkStatus,  a.contractStatus,  a.createMan,  a.orderTime,  a.completeTime ,  a.publisherClearStatus,  a.publisherConfirmStatus,  a.publisherCloseStatus,"
			+ "a.investorClearStatus,a.investorCloseStatus,FORMAT(a.holdVolume,2) as holdVolume,a.redeemStatus,a.accrualStatus,a.beginAccuralDate,a.corpusAccrualEndDate,a.beginRedeemDate,"
			+ "FORMAT(a.totalIncome,2) as totalIncome,FORMAT(a.totalBaseIncome,2) as totalBaseIncome,FORMAT(a.totalRewardIncome,2) as totalRewardIncome,FORMAT(a.yesterdayBaseIncome,2) as yesterdayBaseIncome,"
			+ "FORMAT(a.yesterdayRewardIncome,2) as yesterdayRewardIncome,FORMAT(a.yesterdayIncome,2) as yesterdayIncome,FORMAT(a.toConfirmIncome,2) as toConfirmIncome ,FORMAT(a.incomeAmount,2) as incomeAmount,"
			+ "FORMAT(a.expectIncomeExt,2),FORMAT(a.expectIncome,2),FORMAT(a.value,2),  a.confirmDate,  a.holdStatus,  a.province,  a.city ,  a.updateTime,  a.createTime "
			+ "FROM  t_money_investor_tradeorder a   LEFT JOIN t_money_investor_baseaccount b  ON a.investorOid = b.oid  LEFT JOIN t_gam_product c  ON c.oid=a.productOid  LEFT JOIN t_money_platform_channel ch	on ch.oid=a.channelOid  "
			+ " LEFT JOIN t_money_publisher_baseaccount ba on ba.oid=a.publisherOid LEFT JOIN T_MONEY_CORPORATE co on co.oid=ba.corperateOid "
			+ " left JOIN T_MONEY_PLATFORM_PUBLISHER_OFFSET pu ON a.publisherOffsetOid=pu.oid "
			+ "where ((a.`orderType`='invest' and  a.orderStatus='confirmed') or (a.`orderType` in ('normalRedeem','incrementRedeem') and  a.orderStatus='done')) "
			+ " and ( a.orderType =?1  or ?1 =''||' ')"
			+ " and ( a.orderAmount >=?2  or ?2 =''||' ')"
			+ " and ( a.orderAmount <=?3  or ?3 =''||' ')"
			+ " and ( b.phoneNum =?4  or ?4 =''||' ')"
			+ " and ( b.realName =?5  or ?5 =''||' ')"
			+ " and ( b.idNum =?6  or ?6 =''||' ')"
			+ " and ( a.orderTime >=?7  or ?7 =''||' ')"
			+ " and ( a.orderTime <=?8  or ?8 =''||' ')"
			+ " and ( pu.offsetCode =?9  or ?9 =''||' ')"
//			+ " and  a.orderTime >='2017-2-5 15:00:00'  AND a.`orderTime` < '2017-2-6 15:00:00' "
			+ " limit ?10,?11) b" 
			+ " order by b.createTime desc", nativeQuery = true)
	public List<Object[]> findInvestDetail(String orderType,String minOrderAmount,String maxOrderAmount,String phoneNum,String realName,String idNum,String beginTme,String endTime,String offsetCode,int pages,int rows); 
	//交易明细数据导出
	@Query(value = "select  b.phoneNum  , b.realName ,co.account , c.name,  ch.channelName,  a.investorOffsetOid ,pu.offsetCode,  a.checkOid,  a.holdOid ,  a.orderCode ,  a.orderType,"
			+ "a.orderAmount ,  a.orderVolume ,  a.payAmount,  a.couponAmount,  a.coupons ,  a.couponType,  a.payStatus ,  a.acceptStatus,  a.refundStatus ,  a.orderStatus ,"
			+ "a.checkStatus,  a.contractStatus,  a.createMan,  a.orderTime,  a.completeTime ,  a.publisherClearStatus,  a.publisherConfirmStatus,  a.publisherCloseStatus,"
			+ "a.investorClearStatus,a.investorCloseStatus,FORMAT(a.holdVolume,2) as holdVolume,a.redeemStatus,a.accrualStatus,a.beginAccuralDate,a.corpusAccrualEndDate,a.beginRedeemDate,"
			+ "FORMAT(a.totalIncome,2) as totalIncome,FORMAT(a.totalBaseIncome,2) as totalBaseIncome,FORMAT(a.totalRewardIncome,2) as totalRewardIncome,FORMAT(a.yesterdayBaseIncome,2) as yesterdayBaseIncome,"
			+ "FORMAT(a.yesterdayRewardIncome,2) as yesterdayRewardIncome,FORMAT(a.yesterdayIncome,2) as yesterdayIncome,FORMAT(a.toConfirmIncome,2) as toConfirmIncome ,FORMAT(a.incomeAmount,2) as incomeAmount,"
			+ "FORMAT(a.expectIncomeExt,2),FORMAT(a.expectIncome,2),FORMAT(a.value,2),  a.confirmDate,  a.holdStatus,  a.province,  a.city ,  a.updateTime,  a.createTime "
			+ "FROM  t_money_investor_tradeorder a   LEFT JOIN t_money_investor_baseaccount b  ON a.investorOid = b.oid  LEFT JOIN t_gam_product c  ON c.oid=a.productOid  LEFT JOIN t_money_platform_channel ch	on ch.oid=a.channelOid  "
			+ " LEFT JOIN t_money_publisher_baseaccount ba on ba.oid=a.publisherOid LEFT JOIN T_MONEY_CORPORATE co on co.oid=ba.corperateOid "
			+ " left JOIN T_MONEY_PLATFORM_PUBLISHER_OFFSET pu ON a.publisherOffsetOid=pu.oid "
			+ "where ((a.`orderType`='invest' and  a.orderStatus='confirmed') or (a.`orderType` in ('normalRedeem','incrementRedeem') and  a.orderStatus='done')) "
			+ " and ( a.orderType =?1  or ?1 =''||' ')"
			+ " and ( a.orderAmount >=?2  or ?2 =''||' ')"
			+ " and ( a.orderAmount <=?3  or ?3 =''||' ')"
			+ " and ( b.phoneNum =?4  or ?4 =''||' ')"
			+ " and ( b.realName =?5  or ?5 =''||' ')"
			+ " and ( b.idNum =?6  or ?6 =''||' ')"
			+ " and ( a.orderTime >=?7  or ?7 =''||' ')"
			+ " and ( a.orderTime <=?8  or ?8 =''||' ')"
			+ " and ( pu.offsetCode =?9  or ?9 =''||' ')", nativeQuery = true)
	public List<Object[]> findInvestDetailDown(String orderType,String minOrderAmount,String maxOrderAmount,String phoneNum,String realName,String idNum,String beginTme,String endTime,String offsetCode); 
	
	@Query(value="select * from t_money_investor_tradeorder where orderStatus='confirmed' and productOid=?1", nativeQuery = true)
	public List<InvestorTradeOrderEntity> getConfirmedOrderByProductOid(String productOid);

	// 查询产品下所有某一状态的订单
	@Query(value = "select * from t_money_investor_tradeorder where productOid = ?1 and orderStatus = ?2 ", nativeQuery = true)
	public List<InvestorTradeOrderEntity> findTradeOrderByProductAndOrderStatus(String productOid,String orderStatus); 

	/**
	 * 查询用户的定期订单（不包括新手标和关联竞猜宝活动的产品）
	 * @param investorOid
	 * @return
	 */
	@Query(value = "select count(1) from t_money_investor_tradeorder a, "
			+ " t_gam_product b where a.productOid=b.oid "
			+ " and a.investorOid=?1 "
			+ " and b.type ='PRODUCTTYPE_01' "
			+ " and a.orderStatus in('confirmed','paySuccess','accepted','done') "
			+ " and b.productLabel!='freshman' "
			+ " and b.guessOid is null ", nativeQuery = true)
	public int countTradeOrderTn(String investorOid);
	
	@Query("from InvestorTradeOrderEntity e where e.investorBaseAccount.oid=?1 and ((e.orderType='invest' and e.orderStatus='paySuccess') or (e.orderType in ('normalRedeem','incrementRedeem','bfPlusRedeem') and e.orderStatus in ('submitted', 'confirmed')))")
	public List<InvestorTradeOrderEntity> findOnwayList(String investorOid);
	
 
	@Query(value = "select new com.guohuai.mmp.publisher.hold.TradeOrderHoldDaysDetail(TIMESTAMPDIFF(DAY, A.beginAccuralDate, ?3) + 1, A.holdVolume, A.totalIncome) from InvestorTradeOrderEntity A "
			+ " where A.investorBaseAccount.oid = ?1 AND A.product.oid = ?2 AND A.beginAccuralDate <= ?3 "
			+ " and A.holdStatus in ('holding','partHolding')  AND A.holdVolume > 0")
	public List<TradeOrderHoldDaysDetail> getTradeOrderHoldDaysDetailByInvestor(String investorOid, String productOid, Date incomeDate);
	
	@Query(value="select * from t_money_investor_tradeorder where orderType='cashFailed' and productOid=?1", nativeQuery = true)
	public List<InvestorTradeOrderEntity> getCashFailedOrderByProductOid(String productOid);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryTradeDetailList
	 * @Description: 查询用户交易明细
	 * @param uid
	 * @param t0ProductOid
	 * @param tradeType
	 * @param orderTimeStart
	 * @param orderTimeEnd
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年9月4日 下午8:47:18
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " mit.orderCode, " // 订单号
			+ " mit.orderTime orderTime, " // 交易时间
			+ " CASE WHEN mit.orderType = 'expGoldInvest' AND mit.totalIncome > 0 THEN mit.totalIncome "
			+ " ELSE mit.orderAmount END amount, " // 金额(如果为体验金收益，则显示收益金额，否则，显示订单金额)
			+ " CASE WHEN mit.orderType IN ('invest', 'profitInvest') THEN "
			+ "      1 " // --投资A类(银行卡到快活宝；银行卡到定期；银行卡到暴风天天向上；二级邀请月奖励到快活宝)
			+ "      WHEN mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid <> ?2 THEN "
			+ "      1 " // --投资B类(快活宝到暴风天天向上)
			+ "      WHEN mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_01' THEN "
			+ "      1 " // --投资C类(快活宝到定期)
			+ "      WHEN mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_03' THEN "
			+ "      1 " // --投资D类(快活宝到15天定期)
			+ "      WHEN mit.orderType IN ('normalRedeem', 'incrementRedeem') THEN "
			+ "      2 " // --提现(快活宝到银行卡 ；暴风天天向上到银行卡)
			+ "      WHEN mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid = ?2  THEN "
			+ "      3 " // --回款A类(定期到快活宝；)
			+ "      WHEN mit.orderType = 'writeOff' THEN "
			+ "      3 " // --回款C类(15天定期回款到快活宝)
			+ "      WHEN mit.orderType = 'expGoldInvest' AND mit.totalIncome > 0 THEN "
			+ "      4 " // --体验金收益
			+ " END tradeType " // 交易类型
			+ " FROM t_money_investor_tradeorder mit "
			+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
			+ " WHERE mit.orderStatus NOT IN ('abandoning', 'abandoned') AND mit.orderType NOT IN ('specialRedeem','expGoldRedeem') " // 废单和特殊赎回单不显示
			+ " AND mit.investorOid = ?1 AND ( "
			+ "  CASE WHEN ?3 = 0 THEN " // -- 全部
			+ "       ( "
			+ "         (mit.orderType IN ('invest', 'profitInvest') OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid <> ?2) OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_01') OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_03') ) "
			+ "         OR "
			+ "         mit.orderType IN ('normalRedeem', 'incrementRedeem') "
			+ "         OR "
			+ "         ((mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid = ?2) OR (mit.orderType = 'writeOff') ) "
			+ "         OR "
			+ "         (mit.orderType = 'expGoldInvest' AND mit.totalIncome > 0) "
			+ "       ) "
			+ "       WHEN ?3 = 1 THEN " // -- 投资
			+ "       (mit.orderType IN ('invest', 'profitInvest') OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid <> ?2) OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_01')OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_03') ) "
			+ "       WHEN ?3 = 2 THEN " // -- 提现
			+ "       mit.orderType IN ('normalRedeem', 'incrementRedeem') "
			+ "       WHEN ?3 = 3 THEN " // -- 回款
			+ "       ((mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid = ?2) OR (mit.orderType = 'writeOff')) "
			+ "       WHEN ?3 = 4 THEN " // -- 体验金收益
			+ "       (mit.orderType = 'expGoldInvest' AND mit.totalIncome > 0) "
			+ "  END "
			+ " ) "
			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1 , mit.orderTime >= CONCAT(?4, ' 00:00:00') ) "
			+ " AND IF(?5 IS NULL OR ?5 = '', 1=1 , mit.orderTime <= CONCAT(?5, ' 23:59:59') ) "
			+ " ORDER BY mit.orderTime DESC "
			+ " LIMIT ?6, ?7 ", nativeQuery = true)
	public List<Object[]> queryTradeDetailList(
			String uid, String t0ProductOid, int tradeType, String orderTimeStart, 
			String orderTimeEnd, int pageRow, int row);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryTradeDetailCount
	 * @Description: 查询用户交易明细总条数
	 * @param uid
	 * @param t0ProductOid
	 * @param tradeType
	 * @param orderTimeStart
	 * @param orderTimeEnd
	 * @return int
	 * @date 2017年9月4日 下午8:47:34
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " COUNT(1) "
			+ " FROM t_money_investor_tradeorder mit "
			+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
			+ " WHERE orderStatus NOT IN ('abandoning', 'abandoned') AND mit.orderType NOT IN ('specialRedeem','expGoldRedeem') "
			+ " AND mit.investorOid = ?1 AND ( "
			+ "  CASE WHEN ?3 = 0 THEN " // -- 全部
			+ "       ( "
			+ "         (mit.orderType IN ('invest', 'profitInvest') OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid <> ?2) OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_01') OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_03')) "
			+ "         OR "
			+ "         mit.orderType IN ('normalRedeem', 'incrementRedeem') "
			+ "         OR "
			+ "         ((mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid = ?2) OR (mit.orderType = 'writeOff' )) "
			+ "         OR "
			+ "         (mit.orderType = 'expGoldInvest' AND mit.totalIncome > 0) "
			+ "       ) "
			+ "       WHEN ?3 = 1 THEN " // -- 投资
			+ "       (mit.orderType IN ('invest', 'profitInvest') OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid <> ?2) OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_01')OR (mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_03') ) "
			+ "       WHEN ?3 = 2 THEN " // -- 提现
			+ "       mit.orderType IN ('normalRedeem', 'incrementRedeem') "
			+ "       WHEN ?3 = 3 THEN " // -- 回款
			+ "       ((mit.orderType = 'noPayInvest' AND gp.type = 'PRODUCTTYPE_02' AND mit.productOid = ?2) OR (mit.orderType = 'writeOff') ) "
			+ "       WHEN ?3 = 4 THEN " // -- 体验金收益
			+ "       (mit.orderType = 'expGoldInvest' AND mit.totalIncome > 0) "
			+ "  END "
			+ " ) "
			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1 , mit.orderTime >= CONCAT(?4, ' 00:00:00') ) "
			+ " AND IF(?5 IS NULL OR ?5 = '', 1=1 , mit.orderTime <= CONCAT(?5, ' 23:59:59') ) "
			+ " ORDER BY mit.orderTime DESC ", nativeQuery = true)
	public int queryTradeDetailCount(String uid, String t0ProductOid, int tradeType, 
			String orderTimeStart, String orderTimeEnd);
	
	@Query(value = " SELECT "
			+ " mit.orderCode orderCode, " // 订单号
			+ " DATE_FORMAT(mit.createTime,'%Y-%m-%d %H:%i:%s') createTime, " // 订单创建时间
			+ " mit.orderType orderType, " // 订单类型
			+ " mit.orderStatus orderStatus, " // 订单状态
			+ " mit.orderAmount orderAmount, " // 订单金额
			+ " gp.type productType, " // 产品类型
			+ " gp.name productName, " // 产品名称
			+ " gir.oid reward, " // 是否有奖励
			+ " DATE_FORMAT(mit.completeTime,'%m-%d %H:%i') completeTime, " // 订单完成时间
			+ " mit.relateOid relateOid "
			+ " FROM t_money_investor_tradeorder mit  "
			+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
			+ " LEFT JOIN t_gam_income_reward gir ON mit.productOid = gir.productOid "
			+ " WHERE mit.orderCode = ?1 ", nativeQuery = true)
	public List<Object[]> queryOrderDetail(String orderCode);
	
 
	/**
	 * 查询总金额
	 */
	@Query(value = "select IFNULL(sum(orderAmount),0) from T_MONEY_INVESTOR_TRADEORDER "
			+ " where IF(?1 IS NULL OR ?1= '', 1= 1,orderType = ?1) "
			+ "   And IF(?2 IS NULL OR ?2= '', 1= 1,orderStatus = ?2) "
			+ "   And IF(?3 IS NULL OR ?3= '', 1= 1,isAuto = ?3)"
			+ "   And IF(?4 IS NULL OR ?4= '', 1= 1,orderCode = ?4)"
			+ "   And IF(?5 IS NULL OR ?5= '', 1= 1,createTime>=?5) "
			+ "   And IF(?6 IS NULL OR ?6= '', 1= 1,createTime<=?6) "
			+ "   And IF(?7 IS NULL OR ?7= '', 1= 1, orderAmount>=?7 ) "
			+ "   And IF(?8 IS NULL OR ?8= '', 1= 1, orderAmount<=?8)"
			+ "   And IF(?9 IS NULL OR ?9= '', 1= 1, productOid=?9) "
			+ "	  And IF(?10 IS NULL OR ?10= '', 1= 1, investorOid=?10) ", nativeQuery = true)
	public BigDecimal findTotalAmount(String orderType, String orderStatus,String isAuto,String orderCode,
					String createTimeBegin,String createTimeEnd,BigDecimal minOrderAmount,BigDecimal maxOrderAmount,String productOid,String investorOid);

	@Query(value="SELECT COUNT(1) FROM t_money_investor_tradeorder WHERE investorOid= ?1 AND orderType = 'invest' AND orderStatus IN ('confirmed','paySuccess','accepted')", nativeQuery = true)
	public int countByInvestSuccess(String investorOid);

	@Query(value = "select * FROM T_MONEY_INVESTOR_TRADEORDER t INNER JOIN T_MONEY_INVESTOR_PLUS_REDEEM r ON r.oid = t.oid " +
			" where r.payDate = ?1 AND r.createDate <= ?2 AND t.orderStatus='confirmed' AND status = 0 order by r.createDate DESC limit 200", nativeQuery = true)
	List<InvestorTradeOrderEntity> getBfPlusRedeemList(java.util.Date payDate, java.util.Date createDate);



	/**
	 *
	 *查询需要更新的老订单
	 * @author yujianlong
	 * @date 2018/4/8 17:25
	 * @param [holdOidList]
	 * @return java.util.List<com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity>
	 */
	@Query(value = "SELECT a.* from t_money_investor_tradeorder a "
			+ " where a.holdOid in(?1) and a.orderType in('invest','noPayInvest') and a.holdStatus='holding' and a.orderStatus = 'confirmed' ",nativeQuery = true)
	public List<InvestorTradeOrderEntity> kdbTrades4Update(List<String> holdOidList);



	@Query(value = "UPDATE t_money_investor_tradeorder SET holdVolume = 0, VALUE = 0, holdStatus = 'close' WHERE orderCode IN (?1)", nativeQuery = true)
	@Modifying
	int updateOrder4ContinueInvest(List<String> orderCodes);

	@Query(value = " SELECT * FROM t_money_investor_tradeorder WHERE orderCode IN (?1) ", nativeQuery = true)
	List<InvestorTradeOrderEntity> findByOrderCodes(List<String> orderCodes);

    @Query(value = "UPDATE t_money_investor_tradeorder SET holdStatus = 'closed', holdVolume = 0, redeemStatus = 'no'," +
            " accrualStatus = 'no',orderStatus='invalidate',VALUE = 0 " +
            " WHERE oid = ?1 AND orderStatus = 'confirmed' ", nativeQuery = true)
    @Modifying
	int invalidateTradeOrder(String oid);
}
