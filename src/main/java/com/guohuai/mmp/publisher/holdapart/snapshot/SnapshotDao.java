package com.guohuai.mmp.publisher.holdapart.snapshot;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SnapshotDao extends JpaRepository<SnapshotEntity, String>, JpaSpecificationExecutor<SnapshotEntity> {

	@Query(value = "select * from T_MONEY_PUBLISHER_INVESTOR_HOLD_SNAPSHOT where orderOid = ?1 and snapShotDate = ?2", nativeQuery = true)
	SnapshotEntity findByOrderAndSnapShotDate(String orderOid, Date incomeDate);

	@Modifying
	@Query(value = "update T_MONEY_PUBLISHER_INVESTOR_HOLD_SNAPSHOT set snapshotVolume = snapshotVolume + ?2 "
			+ " where orderOid = ?1 and snapShotDate > ?3 ", nativeQuery = true)
	int increaseSnapshotVolume(String orderOid, BigDecimal holdIncomeVolume, Date incomeDate);

	@Query(value = "select * from T_MONEY_PUBLISHER_INVESTOR_HOLD_SNAPSHOT where holdOid = ?1 and snapShotDate = ?2 and snapShotVolume > 0", nativeQuery = true)
	List<SnapshotEntity> findByHoldOidAndSnapShotDate(String holdOid, Date incomeDate);
	
	@Query(value = "select * from T_MONEY_PUBLISHER_INVESTOR_HOLD_SNAPSHOT where investorOid = ?1 and productOid = ?2 and snapShotDate = ?3 and snapShotVolume > 0", nativeQuery = true)
	List<SnapshotEntity> findByInvestorOidAndProductOidAndSnapShotDate(String investorOid, String productOid, Date incomeDate);

	/**
	 * 复利无奖励收益
	 * 
	 * @param productOid
	 * @param baseIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_money_publisher_investor_hold_snapshot a SET a.baseIncome=TRUNCATE(a.snapshotVolume*?2,8),a.rewardIncome=0,a.holdIncome=a.baseIncome"
			+ " WHERE a.productOid=?1 AND a.snapShotDate=?3", nativeQuery = true)
	int distributeInterestCompoundWithoutRewardIncome(String productOid, BigDecimal baseIncomeRatio, Date incomeDate);

	/**
	 * 复利有奖励收益
	 * 
	 * @param productOid
	 * @param baseIncomeRatio
	 * @param rewardIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_money_publisher_investor_hold_snapshot a SET a.baseIncome=TRUNCATE(a.snapshotVolume*?2,8),"
			+ "a.rewardIncomeRatio=(SELECT b.dratio FROM T_GAM_INCOME_REWARD b WHERE b.productOid=a.productOid AND a.holdDays BETWEEN b.startDate AND b.endDate LIMIT 1),"
			+ "a.rewardIncome=TRUNCATE(IF(a.rewardIncomeRatio>0,a.snapshotVolume*a.rewardIncomeRatio,0),8),a.holdIncome=a.baseIncome+a.rewardIncome,"
			+ "a.rewardRuleOid=(SELECT oid FROM T_GAM_INCOME_REWARD b WHERE b.productOid=a.productOid AND a.holdDays BETWEEN b.startDate AND b.endDate LIMIT 1)"
			+ " WHERE a.productOid=?1 AND a.snapShotDate=?3", nativeQuery = true)
	int distributeInterestCompoundWithRewardIncome(String productOid, BigDecimal baseIncomeRatio, Date incomeDate);

	/**
	 * 一次性付息无奖励收益(关联上用户竞猜活动投资选项)
	 * 
	 * @param productOid
	 * @param fpRate 
	 * @param baseIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_money_publisher_investor_hold_snapshot a,T_MONEY_GUESS_INVEST_ITEM b,T_GAM_GUESS_ITEM c SET a.baseIncome=TRUNCATE(a.snapshotVolume*?2*?4/?5,8),a.rewardIncome=TRUNCATE(a.snapshotVolume*c.percent*?4/?5,8),a.holdIncome=a.baseIncome+a.rewardIncome"
			+ " WHERE  a.orderOid = b.orderOid AND b.itemOid = c.oid AND a.snapShotDate=?3 AND a.productOid=?1", nativeQuery = true)
	int distributeInterestSingleWithoutRewardIncomeForInvestItem(String productOid, BigDecimal fpRate, Date incomeDate,
			int holdDays, int calcBaseDays);
	
	/**
	 * 一次性付息无奖励收益(没关联上用户竞猜活动投资选项)
	 * 
	 * @param productOid
	 * @param baseIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_money_publisher_investor_hold_snapshot a left join t_money_investor_tradeorder_coupon b on a.orderOid=b.orderOid SET a.baseIncome=TRUNCATE(a.snapshotVolume*?2*?4/?5,8),a.rewardIncome=TRUNCATE(a.snapshotVolume*IFNULL(b.interest,0)*IFNULL(b.interestDays,0)/100/?5,2),a.rewardIncomeRatio=IFNULL(b.interest,0),a.holdIncome=a.baseIncome+a.rewardIncome"
			+ " WHERE a.productOid=?1 AND a.snapShotDate=?3", nativeQuery = true)
	int distributeInterestSingleWithoutRewardIncome(String productOid, BigDecimal baseIncomeRatio, Date incomeDate,
			int holdDays, int calcBaseDays);
	/**
	 * 
	 * @author yihonglei
	 * @Title: distributeTnProductInterestIncomeAgain
	 * @Description: 二次计算定期收益
	 * @param productOid
	 * @param incomeDate
	 * @return int
	 * @date 2017年6月12日 下午5:53:05
	 * @since  1.0.0
	 */
	@Modifying
	@Query(value = " UPDATE t_money_publisher_investor_hold_snapshot a, t_money_product_raising_income b SET "
			+ " a.baseIncome=TRUNCATE(a.baseIncome+IFNULL(b.incomeAmount,0),8),a.holdIncome=TRUNCATE(a.holdIncome+IFNULL(b.incomeAmount,0),8),b.status = 'closed' "
			+ " WHERE a.productOid = b.productOid AND a.orderOid = b.orderOid AND b.status='toClose' "
			+ " AND a.productOid=?1 AND a.snapShotDate=?2 ", nativeQuery = true)
	int distributeTnProductInterestIncomeAgain(String productOid, Date incomeDate);
	

	/**
	 * 一次性付息有奖励收益
	 * 
	 * @param productOid
	 * @param baseIncomeRatio
	 * @param rewardIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_money_publisher_investor_hold_snapshot a SET a.baseIncome=TRUNCATE(a.snapshotVolume*?2*?4/?5,8),"
			+ "a.rewardIncomeRatio=(SELECT b.dratio FROM T_GAM_INCOME_REWARD b WHERE b.productOid=a.productOid AND a.holdDays BETWEEN b.startDate AND b.endDate LIMIT 1),"
			+ "a.rewardIncome=TRUNCATE(IF(a.rewardIncomeRatio>0,a.snapshotVolume*a.rewardIncomeRatio,0),8),a.holdIncome=a.baseIncome+a.rewardIncome,"
			+ "a.rewardRuleOid=(SELECT oid FROM T_GAM_INCOME_REWARD b WHERE b.productOid=a.productOid AND a.holdDays BETWEEN b.startDate AND b.endDate LIMIT 1)"
			+ " WHERE a.productOid=?1 AND a.snapShotDate=?3", nativeQuery = true)
	int distributeInterestSingleWithRewardIncome(String productOid, BigDecimal baseIncomeRatio, Date incomeDate,
			int holdDays, int calcBaseDays);

	/**
	 * 清除临时表数据
	 * 
	 * @return
	 */
	@Modifying
	@Query(value = "TRUNCATE TABLE t_money_publisher_investor_hold_snapshot_tmp", nativeQuery = true)
	int truncateSnapshotTmp();

	/**
	 * 将快照表中的计算完收益的数据以投资者维度插入临时表中
	 * 修改holdIncome=baseIncome+rewardIncome解决可能少1分钱问题 2017.10.10
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "INSERT INTO t_money_publisher_investor_hold_snapshot_tmp "
			+ "SELECT REPLACE(UUID(),'-','') oid,a.holdOid,a.productOid,a.investorOid,TRUNCATE(IFNULL(SUM(a.snapshotVolume),0),2) snapshotVolume,a.snapShotDate,TRUNCATE(IFNULL(SUM(a.baseIncome),0),2) baseIncome, TRUNCATE(IFNULL(SUM(a.rewardIncome),0),2) rewardIncome,TRUNCATE(IFNULL(SUM(a.holdIncome),0),2) holdIncome, "
			+ "TRUNCATE(IFNULL(SUM(IF(a.redeemStatus != 'yes',a.holdIncome,0)),0),2) lockHoldIncome, "
			+ "TRUNCATE(IFNULL(SUM(IF(a.redeemStatus = 'yes',a.holdIncome,0)),0),2) redeemableHoldIncome "
			+ "FROM t_money_publisher_investor_hold_snapshot a " + "WHERE a.productOid = ?1 AND a.snapShotDate=?2 "
			+ "GROUP BY a.investorOid,a.holdOid", nativeQuery = true)
//	@Query(value = "INSERT INTO t_money_publisher_investor_hold_snapshot_tmp "
//			+ "SELECT REPLACE(UUID(),'-','') oid,a.holdOid,a.productOid,a.investorOid,TRUNCATE(IFNULL(SUM(a.snapshotVolume),0),2) snapshotVolume,a.snapShotDate,TRUNCATE(IFNULL(SUM(a.baseIncome),0),2) baseIncome, TRUNCATE(IFNULL(SUM(a.rewardIncome),0),2) rewardIncome,(TRUNCATE(IFNULL(SUM(a.baseIncome),0),2)+TRUNCATE(IFNULL(SUM(a.rewardIncome),0),2)) holdIncome, "
//			+ "TRUNCATE(IFNULL(SUM(IF(a.redeemStatus != 'yes',a.holdIncome,0)),0),2) lockHoldIncome, "
//			+ "TRUNCATE(IFNULL(SUM(IF(a.redeemStatus = 'yes',a.holdIncome,0)),0),2) redeemableHoldIncome "
//			+ "FROM t_money_publisher_investor_hold_snapshot a " + "WHERE a.productOid = ?1 AND a.snapShotDate=?2 "
//			+ "GROUP BY a.investorOid,a.holdOid", nativeQuery = true)
	int insertIntoSnapshotTmp(String productOid, Date incomeDate);

	/**
	 * 根据订单表分发收益
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE T_MONEY_INVESTOR_TRADEORDER a,t_money_publisher_investor_hold_snapshot b SET "
			+ "a.holdVolume=a.holdVolume+b.holdIncome/?3, " + "a.totalIncome=a.totalIncome+b.holdIncome, "
			+ "a.totalBaseIncome=a.totalBaseIncome+b.baseIncome, "
			+ "a.totalRewardIncome=a.totalRewardIncome+b.rewardIncome, " + "a.yesterdayBaseIncome=b.baseIncome, "
			+ "a.yesterdayRewardIncome=b.rewardIncome, " + "a.yesterdayIncome=b.holdIncome, "
			+ "a.incomeAmount=b.holdIncome, " + "a.value=a.value+b.holdIncome, " + "a.confirmDate=b.snapShotDate "
			+ "WHERE a.productOid=b.productOid AND a.oid=b.orderOid AND b.snapShotDate=?2 AND (a.confirmDate != b.snapShotDate OR a.confirmDate IS NULL) "
			+ "AND b.productOid=?1", nativeQuery = true)
	int distributeOrderInterest(String productOid, Date incomeDate, BigDecimal netUnitShare);

	/**
	 * 是否已经根据持有人手册表分发收益
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Query(value = "SELECT COUNT(*) FROM t_money_publisher_hold b WHERE b.productOid=?1 AND b.confirmDate=?2 LIMIT 1", nativeQuery = true)
	int hasdistributedHoldInterest(String productOid, Date incomeDate);

	/**
	 * 根据持有人手册表分发收益
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_money_publisher_hold a,t_money_publisher_investor_hold_snapshot_tmp b SET "
			+ "a.totalVolume=a.totalVolume+b.holdIncome/?3, " + "a.holdVolume=a.holdVolume+b.holdIncome/?3, "
			+ "a.lockRedeemHoldVolume=a.lockRedeemHoldVolume+b.lockHoldIncome/?3, "
			+ "a.redeemableHoldVolume=a.redeemableHoldVolume+b.redeemableHoldIncome/?3, "
			+ "a.accruableHoldVolume=a.accruableHoldVolume+b.holdIncome/?3, "
			+ "a.holdTotalIncome=a.holdTotalIncome+b.holdIncome, "
			+ "a.totalBaseIncome=a.totalBaseIncome+b.baseIncome, "
			+ "a.totalRewardIncome=a.totalRewardIncome+b.rewardIncome, " + "a.yesterdayBaseIncome=b.baseIncome, "
			+ "a.yesterdayRewardIncome=b.rewardIncome, " + "a.holdYesterdayIncome=b.holdIncome, "
			+ "a.incomeAmount=b.holdIncome, " + "a.value=a.value+b.holdIncome, " + "a.confirmDate=b.snapShotDate "
			+ "WHERE a.productOid=b.productOid AND a.oid=b.holdOid AND b.snapShotDate=?2 "
			+ "AND b.productOid=?1", nativeQuery = true)
	int distributeHoldInterest(String productOid, Date incomeDate, BigDecimal netUnitShare);

	/**
	 * 收益更新到投资统计信息
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE T_MONEY_INVESTOR_STATISTICS a,t_money_publisher_investor_hold_snapshot_tmp b SET "
			+ "a.totalIncomeAmount=a.totalIncomeAmount+b.holdIncome, "
			+ "a.t0YesterdayIncome=IF('PRODUCTTYPE_02' = ?3,b.holdIncome,a.t0YesterdayIncome), "
			+ "a.tnTotalIncome=IF('PRODUCTTYPE_01' = ?3,a.tnTotalIncome+b.holdIncome,a.tnTotalIncome), "
			+ "a.t0TotalIncome=IF('PRODUCTTYPE_02' = ?3,a.t0TotalIncome+b.holdIncome,a.t0TotalIncome), "
			+ "a.t0CapitalAmount=IF('PRODUCTTYPE_02' = ?3,a.t0CapitalAmount+b.holdIncome,a.t0CapitalAmount), "
			+ "a.tnCapitalAmount=IF('PRODUCTTYPE_01' = ?3,a.tnCapitalAmount+b.holdIncome,a.tnCapitalAmount), "
			+ "a.incomeConfirmDate=b.snapShotDate "
			+ "WHERE a.investorOid=b.investorOid AND b.snapShotDate=?2 "
			+ "AND b.productOid=?1", nativeQuery = true)
	int distributeInterestToInvestorStatistic(String productOid, Date incomeDate, String ptype);

	/**
	 * 收益更新到产品表
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_gam_product a SET a.currentVolume=a.currentVolume+ "
			+ "(SELECT TRUNCATE(IFNULL(SUM(b.holdIncome),0),2) FROM t_money_publisher_investor_hold_snapshot b WHERE b.productOid=a.oid AND b.snapShotDate=?2), "
			+ "a.newestProfitConfirmDate=?2 "
			+ "WHERE a.oid=?1 AND (a.newestProfitConfirmDate !=?2 OR a.newestProfitConfirmDate IS NULL)", nativeQuery = true)
	int distributeInterestToProduct(String productOid, Date incomeDate);

	/**
	 * 是否已经发放收益到投资者收益明细（订单粒度）
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Query(value = "SELECT COUNT(*) FROM T_MONEY_PUBLISHER_INVESTOR_INCOME b WHERE b.productOid=?1 AND b.confirmDate=?2 LIMIT 1", nativeQuery = true)
	int hasDistributedInterestToInvestorIncome(String productOid, Date incomeDate);

	/**
	 * 发放收益到投资者收益明细（订单粒度）
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @param incomeOid
	 * @return
	 */
	@Modifying
	@Query(value = "INSERT INTO T_MONEY_PUBLISHER_INVESTOR_INCOME( oid,holdOid,productOid,investorOid,incomeOid,holdIncomeOid,rewardRuleOid,levelIncomeOid,orderOid,incomeAmount,baseAmount,rewardAmount,accureVolume,confirmDate) "
			+ "SELECT REPLACE(UUID(),'-','') oid,b.holdOid,b.productOid,b.investorOid,?3,null,b.rewardRuleOid,null,b.orderOid,b.holdIncome,b.baseIncome,b.rewardIncome,b.snapshotVolume,b.snapShotDate FROM t_money_publisher_investor_hold_snapshot b "
			+ "WHERE b.productOid = ?1 AND b.snapShotDate=?2", nativeQuery = true)
	int distributeInterestToInvestorIncome(String productOid, Date incomeDate, String incomeOid);

	/**
	 * 是否已经发放收益到投资者阶梯奖励收益明细(投资者阶梯奖励收益粒度)
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Query(value = "SELECT COUNT(*) FROM T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME b WHERE b.productOid=?1 AND b.confirmDate=?2 LIMIT 1", nativeQuery = true)
	int hasDistributedInterestToInvestorLevelIncome(String productOid, Date incomeDate);

	/**
	 * 发放收益到投资者阶梯奖励收益明细(投资者阶梯奖励收益粒度)
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Modifying
	@Query(value = "INSERT INTO T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME(oid,holdOid,productOid,rewardRuleOid,investorOid,holdIncomeOid,incomeAmount,baseAmount,rewardAmount,accureVolume,VALUE,confirmDate) "
			+ "SELECT REPLACE(UUID(),'-','') oid,holdOid,productOid,rewardRuleOid,investorOid,holdIncomeOid,TRUNCATE(IFNULL(SUM(incomeAmount),0),2),TRUNCATE(IFNULL(SUM(baseAmount),0),2),TRUNCATE(IFNULL(SUM(rewardAmount),0),2),TRUNCATE(IFNULL(SUM(accureVolume),0),2),TRUNCATE(IFNULL(SUM(incomeAmount/?3),0),2),confirmDate FROM T_MONEY_PUBLISHER_INVESTOR_INCOME b "
			+ "WHERE b.productOid=?1 AND b.confirmDate=?2 "
			+ "GROUP BY b.investorOid,b.rewardRuleOid", nativeQuery = true)
	int distributeInterestToInvestorLevelIncome(String productOid, Date incomeDate, BigDecimal netUnitShare);

	/**
	 * 是否已经发放收益到投资者合仓收益明细（投资者合仓粒度）
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Query(value = "SELECT COUNT(*) FROM T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME b WHERE b.productOid=?1 AND b.confirmDate=?2 LIMIT 1", nativeQuery = true)
	int hasDistributedInterestToInvestorHoldIncome(String productOid, Date incomeDate);

	/**
	 * 发放收益到投资者合仓收益明细（投资者合仓粒度）
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "INSERT INTO T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME(oid,holdOid,productOid,incomeOid,investorOid,incomeAmount,baseAmount,rewardAmount,accureVolume,confirmDate) "
			+ "SELECT REPLACE(UUID(),'-','') oid,holdOid,productOid,incomeOid,investorOid,TRUNCATE(IFNULL(SUM(incomeAmount),0),2),TRUNCATE(IFNULL(SUM(baseAmount),0),2),TRUNCATE(IFNULL(SUM(rewardAmount),0),2),TRUNCATE(IFNULL(SUM(accureVolume),0),2),confirmDate FROM T_MONEY_PUBLISHER_INVESTOR_INCOME b "
			+ "WHERE b.productOid=?1 AND b.confirmDate=?2 " + "GROUP BY b.investorOid", nativeQuery = true)
	int distributeInterestToInvestorHoldIncome(String productOid, Date incomeDate);

	/**
	 * 是否已经再次更新投资者收益明细的引用holdIncomeOid，levelIncomeOid
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Query(value = "SELECT COUNT(*) FROM T_MONEY_PUBLISHER_INVESTOR_INCOME b WHERE b.productOid=?1 AND b.confirmDate=?2 and b.holdIncomeOid is not null and b.levelIncomeOid is not null LIMIT 1", nativeQuery = true)
	int hasReupdatedInvestorIncomeWithRewardIncome(String productOid, Date incomeDate);

	/**
	 * 再次更新投资者收益明细的引用holdIncomeOid，levelIncomeOid
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE T_MONEY_PUBLISHER_INVESTOR_INCOME a SET "
			+ "a.holdIncomeOid=(SELECT b.oid FROM T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME b WHERE b.productOid=a.productOid AND b.investorOid=a.investorOid AND b.confirmDate=a.confirmDate), "
			+ "a.levelIncomeOid=(SELECT c.oid FROM T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME c WHERE c.productOid=a.productOid AND c.investorOid=a.investorOid AND c.rewardRuleOid=a.rewardRuleOid AND c.confirmDate=a.confirmDate) "
			+ "WHERE a.productOid = ?1 AND a.confirmDate=?2", nativeQuery = true)
	int reupdateInvestorIncomeWithRewardIncome(String productOid, Date incomeDate);

	/**
	 * 是否已经再次更新投资者收益明细的引用holdIncomeOid
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Query(value = "SELECT COUNT(*) FROM T_MONEY_PUBLISHER_INVESTOR_INCOME b WHERE b.productOid=?1 AND b.confirmDate=?2 and b.holdIncomeOid is not null LIMIT 1", nativeQuery = true)
	int hasReupdatedInvestorIncomeWithoutRewardIncome(String productOid, Date incomeDate);

	/**
	 * 再次更新投资者收益明细的引用holdIncomeOid
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE T_MONEY_PUBLISHER_INVESTOR_INCOME a SET "
			+ "a.holdIncomeOid=(SELECT b.oid FROM T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME b WHERE b.productOid=a.productOid AND b.investorOid=a.investorOid AND b.confirmDate=a.confirmDate) "
			+ "WHERE a.productOid = ?1 AND a.confirmDate=?2", nativeQuery = true)
	int reupdateInvestorIncomeWithoutRewardIncome(String productOid, Date incomeDate);

	/**
	 * 获取已分派收益信息
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Query(value = "SELECT IFNULL(SUM(b.holdYesterdayIncome),0),IFNULL(SUM(b.yesterdayRewardIncome),0),IFNULL(SUM(b.yesterdayBaseIncome),0),COUNT(b.investorOid) FROM t_money_publisher_hold b "
			+ "WHERE b.productOid=?1 AND b.confirmDate=?2", nativeQuery = true)
	List<Object[]> getDistributedInterestInfo(String productOid, Date incomeDate);

	/**
	 * 试算有奖励收益
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "INSERT INTO T_MONEY_PUBLISHER_PRODUCT_REWARDINCOMEPRACTICE(oid,productOid,rewardRuleOid,totalHoldVolume,totalRewardIncome,tDate) "
			+ "SELECT REPLACE(UUID(),'-','') oid,?1,a.rewardRuleOid,TRUNCATE(IFNULL(SUM(a.snapshotVolume),0),2),TRUNCATE(IFNULL(SUM(a.rewardIncome),0),2),?2 FROM t_money_publisher_investor_hold_snapshot a  "
			+ "WHERE a.productOid = ?1 AND a.snapShotDate=?2 " + "GROUP BY a.rewardRuleOid", nativeQuery = true)
	int practiceDistributeInterestWithRewardIncome(String productOid, Date incomeDate);

	/**
	 * 试算没有奖励收益
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "INSERT INTO T_MONEY_PUBLISHER_PRODUCT_REWARDINCOMEPRACTICE(oid,productOid,rewardRuleOid,totalHoldVolume,totalRewardIncome,tDate) "
			+ "SELECT REPLACE(UUID(),'-','') oid,?1,null,TRUNCATE(IFNULL(SUM(a.snapshotVolume),0),2),null,?2 FROM t_money_publisher_investor_hold_snapshot a  "
			+ "WHERE a.productOid = ?1 AND a.snapShotDate=?2 ", nativeQuery = true)
	int practiceDistributeInterestWithoutRewardIncome(String productOid, Date incomeDate);

	/**
	 * 获取大于指定快照日期之后已拍的快照日期
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Query(value = "SELECT a.snapShotDate FROM t_money_publisher_investor_hold_snapshot a WHERE " + "a.productOid = ?1 "
			+ "AND a.snapShotDate>?2 " + "GROUP BY a.snapShotDate " + "ORDER BY a.snapShotDate", nativeQuery = true)
	List<Date> getAfterIncomeDate(String productOid, Date incomeDate);

	/**
	 * 重新同步在派发收益日期之后已经拍过快照的数据
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @param afterIncomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_money_publisher_investor_hold_snapshot a,t_money_publisher_investor_hold_snapshot b "
			+ "SET b.snapshotVolume=b.snapshotVolume+a.holdIncome/?4 "
			+ "WHERE a.productOid=b.productOid AND a.orderOid=b.orderOid " + "AND a.snapShotDate=?2 "
			+ "AND b.snapShotDate=?3 " + "AND a.productOid = ?1", nativeQuery = true)
	int reupdateAfterIncomeDateSnapshot(String productOid, Date incomeDate, Date afterIncomeDate,
			BigDecimal netUnitShare);

	/**发放体验金收益到订单表
	 * @param productOid
	 * @param incomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE T_MONEY_INVESTOR_TRADEORDER a,t_money_publisher_investor_hold_snapshot b SET "
			+ "a.holdVolume=a.holdVolume+b.holdIncome/?3, " + "a.totalIncome=a.totalIncome+b.holdIncome, "
			+ "a.totalBaseIncome=a.totalBaseIncome+b.baseIncome, "
			+ "a.totalRewardIncome=a.totalRewardIncome+b.rewardIncome, " + "a.yesterdayBaseIncome=b.baseIncome, "
			+ "a.yesterdayRewardIncome=b.rewardIncome, " + "a.yesterdayIncome=b.holdIncome, "
			+ "a.incomeAmount=b.holdIncome, " + "a.value=a.value+b.holdIncome, " + "a.confirmDate=b.snapShotDate "
			+ "WHERE a.productOid=b.productOid AND a.oid=b.orderOid AND b.snapShotDate=?2 AND (a.confirmDate != b.snapShotDate OR a.confirmDate IS NULL) "
			+ "AND b.productOid=?1", nativeQuery = true)
	int distributeOrderInterestForTasteCoupon(String productOid, Date incomeDate, BigDecimal netUnitShare);

	/**体验金复利无奖励收益
	 * @param oid
	 * @param baseIncomeRatio
	 * @param incomeDate
	 * @return
	 */
	@Modifying
	@Query(value = "UPDATE t_money_publisher_investor_hold_snapshot a SET a.baseIncome=TRUNCATE(a.snapshotVolume*?2,2),a.rewardIncome=0,a.holdIncome=a.baseIncome"
			+ " WHERE a.productOid=?1 AND a.snapShotDate=?3", nativeQuery = true)
	int distributeInterestCompoundWithoutRewardIncomeForTasteCoupon(String oid, BigDecimal baseIncomeRatio,
			Date incomeDate);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryTnRaiseIncomeCount
	 * @Description: 检查募集期收益明细中是否已经生成过募集期收益发放明细
	 * @param productOid
	 * @return int
	 * @date 2017年6月12日 下午4:53:16
	 * @since  1.0.0
	 */
	@Query(value = " SELECT COUNT(1) FROM t_money_product_raising_income WHERE productOid = ?1 LIMIT 1 ", nativeQuery = true)
	int queryTnRaiseIncomeCount(String productOid);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: updateTnRaisingIncome
	 * @Description: 更新定期募集期收益发放明细
	 * @param productOid
	 * @return int
	 * @date 2017年6月12日 下午4:30:01
	 * @since  1.0.0
	 */
	@Modifying
	@Query(value = " UPDATE t_money_product_raising_income tn,t_money_publisher_investor_hold_snapshot snap SET "
			+ " tn.incomeAmount = (tn.incomeAmount + snap.holdIncome) "
			+ " WHERE tn.productOid = snap.productOid AND tn.orderOid = snap.orderOid "
			+ " AND tn.productOid = ?1 ", nativeQuery = true)
	int updateTnRaisingIncome(String productOid);
	/**
	 * 
	 * @author yihonglei
	 * @Title: initTnRaisingIncome
	 * @Description: 首次生成定期存续期收益发放明细
	 * @param productOid
	 * @param incomeDate
	 * @return int
	 * @date 2017年6月12日 下午4:30:39
	 * @since  1.0.0
	 */
	@Modifying
	@Query(value = " INSERT INTO t_money_product_raising_income "
			+ " SELECT REPLACE(UUID(),'-',''),snap.investorOid,snap.productOid,snap.orderOid,snap.holdIncome, p.recPeriodExpAnYield,'toClose',SYSDATE(),SYSDATE() "
			+ " FROM t_gam_product p LEFT JOIN t_money_publisher_investor_hold_snapshot snap ON p.oid = snap.productOid "
			+ " WHERE snap.productOid = ?1 AND snapShotDate = ?2 AND p.recPeriodExpAnYield > 0 "
			+ " AND snap.orderOid NOT IN ( SELECT orderOid FROM t_money_product_raising_income tn WHERE  tn.productOid = ?1 ) ", nativeQuery = true)
	int initTnRaisingIncome(String productOid, Date incomeDate);

	/**
	 * 循环开放产品发放收益到投资者合仓收益明细
	 * @return
	 */
	@Modifying
	@Query(value = " INSERT INTO T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME (oid, holdOid, productOid, investorOid, incomeAmount, baseAmount, rewardAmount, accureVolume, confirmDate) " +
			"  SELECT " +
			"    REPLACE(UUID(), '-', '') oid, " +
			"    b.holdOid, " +
			"    b.productOid, " +
			"    b.investorOid, " +
			"    TRUNCATE(IFNULL(b.totalIncome, 0), 2), " +
			"    TRUNCATE(IFNULL(b.totalIncome, 0), 2), " +
			"    0, " +
			"    TRUNCATE(IFNULL(b.orderAmount, 0), 2), " +
			"    b.confirmDate " +
			"  FROM t_gam_product a, t_money_investor_tradeorder b " +
			"  WHERE a.oid = b.productOid " +
			"        AND a.type = 'PRODUCTTYPE_04' " +
			"        AND a.repayInterestStatus = 'repaying' " +
			"        AND a.repayLoanStatus = 'toRepay' " +
			"        AND b.orderType IN ('changeInvest', 'continueInvest') AND b.orderStatus = 'confirmed'", nativeQuery = true)
	int distributeCycleProductInterestToInvestorHoldIncome();

	/**
	 * 循环开放产品发放收益到收益明细
	 * @return
	 */
	@Modifying
	@Query(value = " INSERT INTO T_MONEY_PUBLISHER_INVESTOR_INCOME ( " +
			"  oid, holdOid, productOid, investorOid, " +
			"  orderOid, incomeAmount, baseAmount, " +
			"  accureVolume, confirmDate, holdIncomeOid, rewardAmount) " +
			"  SELECT " +
			"    REPLACE(UUID(), '-', '') oid, " +
			"    b.holdOid, " +
			"    b.productOid, " +
			"    b.investorOid, " +
			"    b.oid, " +
			"    b.totalIncome, " +
			"    b.totalIncome, " +
			"    b.orderAmount, " +
			"    b.confirmDate, " +
			"    c.oid, " +
			"	 0 " +
			"  FROM t_gam_product a, t_money_investor_tradeorder b, t_money_publisher_investor_holdincome c " +
			"  WHERE a.oid = b.productOid AND b.productOid = c.productOid AND b.investorOid = c.investorOid " +
			"        AND b.confirmDate = c.confirmDate " +
			"        AND a.type = 'PRODUCTTYPE_04' " +
			"        AND a.repayInterestStatus = 'repaying' " +
			"        AND a.repayLoanStatus = 'toRepay' " +
			"        AND b.orderType IN ('changeInvest', 'continueInvest') AND b.orderStatus = 'confirmed'", nativeQuery = true)
	int distributeCycleProductInterestToInvestorIncome();

	/**
	 * 循环开放产品发放收益到投资者统计信息表
	 * @return
	 */
	@Query(value = " UPDATE t_gam_product a, t_money_publisher_hold b, t_money_investor_statistics c " +
			" SET c.totalIncomeAmount = c.totalIncomeAmount + b.totalBaseIncome " +
			" WHERE a.oid = b.productOid AND b.investorOid = c.investorOid AND a.type = 'PRODUCTTYPE_04' AND " +
			"      a.repayInterestStatus = 'repaying' AND a.repayLoanStatus = 'toRepay' ", nativeQuery = true)
	@Modifying
	int distributeCycleProductInterestToStatisticsIncome();

	/**
	 * 循环开放产品发放收益到投资者合仓收益明细
	 * @return
	 */
	@Modifying
	@Query(value = " INSERT INTO T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME (oid, holdOid, productOid, investorOid, " +
            "incomeAmount, baseAmount, rewardAmount, accureVolume, confirmDate) " +
			"VALUES (REPLACE(UUID(), '-', ''),?1,?2,?3,?4,?4,0,?5,now())" +
            " on DUPLICATE KEY UPDATE incomeAmount = incomeAmount + ?4, baseAmount = baseAmount + ?4, accureVolume = accureVolume - ?5",
            nativeQuery = true)
	int distributeCycleProductInterest(String holdOid, String productOid, String investorOid, BigDecimal totalIncome, BigDecimal orderAmount);

    @Modifying
    @Query(value = "UPDATE T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME SET incomeAmount = incomeAmount + ?2, " +
            "baseAmount = baseAmount + ?2, accureVolume = accureVolume - ?3 WHERE oid = ?1",
            nativeQuery = true)
    int updateCycleProductInterest(String oid, BigDecimal totalIncome, BigDecimal orderAmount);

    @Modifying
    @Query(value = " INSERT INTO T_MONEY_PUBLISHER_INVESTOR_INCOME ( " +
            "  oid, holdOid, productOid, investorOid, " +
            "  orderOid, incomeAmount, baseAmount, " +
            "  accureVolume, confirmDate, holdIncomeOid, rewardAmount) " +
            "  SELECT " +
            "    REPLACE(UUID(), '-', '') oid, " +
            "    b.holdOid, " +
            "    b.productOid, " +
            "    b.investorOid, " +
            "    b.oid, " +
            "    b.totalIncome, " +
            "    b.totalIncome, " +
            "    ?2, " +
            "    b.confirmDate, " +
            "    c.oid, " +
            "	 0 " +
            "  FROM t_money_investor_tradeorder b, t_money_publisher_investor_holdincome c " +
            "  WHERE b.oid = ?1 AND c.productOid = b.productOid AND c.investorOid = b.investorOid " +
            "        AND c.confirmDate = b.confirmDate ", nativeQuery = true)
    int distributeCycleProductInterestIncome(String redeemOrderOid, BigDecimal orderAmount);
}
