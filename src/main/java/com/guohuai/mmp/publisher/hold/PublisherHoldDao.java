package com.guohuai.mmp.publisher.hold;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.product.Product;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface PublisherHoldDao extends JpaRepository<PublisherHoldEntity, String>, JpaSpecificationExecutor<PublisherHoldEntity> {

	public PublisherHoldEntity findByInvestorBaseAccountAndProduct(InvestorBaseAccountEntity investorBaseAccount, Product product);
	
	/**
	 * 投资后更新持有人
	 * @param oid 持有人名录oid
	 * @param netUnitShare 单位净值
	 * @param volume 投资份额
	 * 增加<<总份额totalVolume>> <<待确认份额toConfirmVolume>><<累计投资份额totalInvestVolume>>
	 * 增加<<市值value>>
	 */
	@Query(value = "update PublisherHoldEntity set "
			+ " totalVolume = totalVolume + ?3, toConfirmInvestVolume = toConfirmInvestVolume + ?3,"
			+ " totalInvestVolume = totalInvestVolume + ?3, "
			+ " dayInvestVolume = dayInvestVolume + ?3, "
			+ " value = totalVolume * ?2, expectIncome = expectIncome + ?4,  expectIncomeExt = expectIncomeExt + ?5 where oid = ?1")
	@Modifying
	public int invest(String holdOid, BigDecimal netUnitShare, BigDecimal volume, BigDecimal expectIncome, BigDecimal expectIncomeExt);
	
	@Query(value = "update PublisherHoldEntity set totalInvestVolume = totalInvestVolume + ?3, "
			+ "totalVolume = totalVolume + ?3, toConfirmInvestVolume = toConfirmInvestVolume + ?3, dayInvestVolume = dayInvestVolume + ?3,"
			+ "value = totalVolume * ?2 where oid = ?1")
	@Modifying
	public int writerOffOrder(String holdOid, BigDecimal netUnitShare, BigDecimal volume);

	
	/**
	 * 体验金入仓 
	 */
	@Query(value = "update PublisherHoldEntity set totalInvestVolume = totalInvestVolume + ?3, dayInvestVolume = dayInvestVolume + ?3, "
			+ " totalVolume = totalVolume + ?3, holdVolume = holdVolume + ?3, expGoldVolume = expGoldVolume + ?3, "
			+ " accruableHoldVolume = accruableHoldVolume + ?3,"
			+ " value = totalVolume * ?2 where oid = ?1")
	@Modifying
	public void investGold(String oid, BigDecimal netUnitShare, BigDecimal orderVolume);
	
	
	@Query(value = "update PublisherHoldEntity set totalInvestVolume = totalInvestVolume + ?3, "
			+ " totalVolume = totalVolume + ?3, "
			+ " holdVolume = holdVolume + ?3, "
			+ " accruableHoldVolume = accruableHoldVolume + ?4, redeemableHoldVolume = redeemableHoldVolume + ?3,"
			+ " value = totalVolume * ?2 where oid = ?1")
	@Modifying
	public void invest4Super(String oid, BigDecimal netUnitShare, BigDecimal orderVolume, BigDecimal accruableHoldVolume);
	
	/**
	 * 通过注册表重新检测，增加可计息份额，减少计息锁定份额
	 * @param oid 持有人主键
	 * @param volume 份额
	 * @return
	 */
	@Query(value = "update PublisherHoldEntity set accruableHoldVolume = accruableHoldVolume + ?2  where oid = ?1 ")
	@Modifying
	public int unlockAccrual(String holdOid, BigDecimal volume);
	
	/**
	 * 赎回锁定期到了
	 * 增加可赎回份额，减少赎回锁定份额
	 * @param oid 持有人主键
	 * @param volume 份额
	 * @return
	 */
	@Query(value = "update PublisherHoldEntity set lockRedeemHoldVolume = lockRedeemHoldVolume - ?2,"
			+ " redeemableHoldVolume = redeemableHoldVolume + ?2 where oid = ?1 and lockRedeemHoldVolume - ?2 >= 0")
	@Modifying
	public int unlockRedeem(String holdOid, BigDecimal volume);
	
	@Query(value = "update PublisherHoldEntity set expGoldVolume = expGoldVolume - ?2,"
			+ " redeemableHoldVolume = redeemableHoldVolume + ?2 + ?3, lockRedeemHoldVolume = lockRedeemHoldVolume - ?3 "
			+ " where oid = ?1 and expGoldVolume - ?2 >= 0")
	@Modifying
	public int unlockExpGoldVolume(String holdOid, BigDecimal orderVolume, BigDecimal totalIncome);
	
	
	/**
	 * 赎回确认
	 */
//	@Query(value = "update T_MONEY_PUBLISHER_HOLD set totalVolume = totalVolume - ?1, "
//			+ " maxHoldVolume = IF (maxHoldVolume >= ?1, maxHoldVolume - ?1, 0),"
//			+ " accruableHoldVolume = IF(accruableHoldVolume >= ?1, accruableHoldVolume - ?1, 0) , "
//			+ " toConfirmRedeemVolume = toConfirmRedeemVolume - ?1, "
//			+ " holdVolume = holdVolume - ?1, "
//			+ " value = totalVolume * ?2"
//			+ " where investorOid = ?3 and productOid = ?4 "
//			+ "and toConfirmRedeemVolume >= ?1 and totalVolume >= ?1 and holdVolume >= ?1", nativeQuery = true)
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set totalVolume = totalVolume - ?1, "
			+ " maxHoldVolume = IF (maxHoldVolume >= ?1, maxHoldVolume - ?1, 0),"
			+ " accruableHoldVolume = IF(accruableHoldVolume >= ?1, accruableHoldVolume - ?1, 0) , "
			+ " redeemableHoldVolume = redeemableHoldVolume - ?1, "
			+ " holdVolume = holdVolume - ?1, "
			+ " value = totalVolume * ?2, holdStatus = ?5"
			+ " where investorOid = ?3 and productOid = ?4 "
			+ " and redeemableHoldVolume >= ?1 and totalVolume >= ?1 and holdVolume >= ?1", nativeQuery = true)
	@Modifying
	public int normalRedeem(BigDecimal volume, BigDecimal netUnitShare, String investorOid, String productOid, String holdStatus);

	@Query(value = "update T_MONEY_PUBLISHER_HOLD set "
//			+ " maxHoldVolume = IF (maxHoldVolume >= ?1, maxHoldVolume - ?1, 0),"
			+ " accruableHoldVolume = IF(accruableHoldVolume >= ?1, accruableHoldVolume - ?1, 0), "
			+ " redeemableHoldVolume = redeemableHoldVolume - ?1, lockRedeemHoldVolume = lockRedeemHoldVolume + ?1,"
			+ " totalVolume = totalVolume - ?1, holdVolume = holdVolume - ?1, value = totalVolume * ?2,"
            + " expectIncome = ?5, expectIncomeExt = ?5, "
            + " holdTotalIncome = holdTotalIncome + ?6, totalBaseIncome = totalBaseIncome + ?6, yesterdayBaseIncome = yesterdayBaseIncome + ?6,"
			+ " incomeAmount = incomeAmount + ?6, confirmDate = now(), holdStatus = IF(totalVolume - ?1 = 0 , 'closed', holdStatus)"
			+ " where investorOid = ?3 and productOid = ?4 "
			+ " and redeemableHoldVolume >= ?1 and totalVolume >= ?1 and holdVolume >= ?1", nativeQuery = true)
	@Modifying
	int bfPlusRedeem(BigDecimal volume, BigDecimal netUnitShare, String investorOid, String productOid, BigDecimal expectIncome,BigDecimal income);

	@Query(value = "update T_MONEY_PUBLISHER_HOLD set "
			+ " lockRedeemHoldVolume = IF(lockRedeemHoldVolume >= ?1, lockRedeemHoldVolume - ?1, 0), "
			+ " holdStatus = IF(totalVolume - ?1 = 0 , 'closed', ?4)"
			+ " where investorOid = ?2 and productOid = ?3 and lockRedeemHoldVolume >= ?1", nativeQuery = true)
	@Modifying
	int bfPlusRedeemDone(BigDecimal volume, String investorOid, String productOid, String holdStatus);

	@Query(value = "update T_MONEY_PUBLISHER_HOLD set totalVolume = totalVolume + ?1, "
			+ " maxHoldVolume = maxHoldVolume + ?1,"
			+ " accruableHoldVolume = accruableHoldVolume + ?1 , "
			+ " redeemableHoldVolume = redeemableHoldVolume + ?1, "
			+ " holdVolume = holdVolume + ?1, "
			+ " value = totalVolume * ?2 "
			+ " where investorOid = ?3 and productOid = ?4 ", nativeQuery = true)
	@Modifying
	public int normalRedeemFailed(BigDecimal volume, BigDecimal netUnitShare, String investorOid, String productOid);
	
	
	/**
	 * 体验金赎回确认 
	 */
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set totalVolume = totalVolume - ?1 - ?5, "
			+ " accruableHoldVolume = IF(accruableHoldVolume >= ?1 + ?5, accruableHoldVolume - ?1 - ?5, 0) , "
			+ " expGoldVolume = expGoldVolume - ?1, "
			+ " holdVolume = holdVolume - ?1 - ?5, "
			+ " lockRedeemHoldVolume = lockRedeemHoldVolume - ?5, "
			+ " value = totalVolume * ?2"
			+ " where investorOid = ?3 and productOid = ?4 "
			+ " and expGoldVolume >= ?1 and totalVolume >= ?1 and holdVolume >= ?1", nativeQuery = true)
	@Modifying
	public int expGoldRedeem(BigDecimal volume, BigDecimal netUnitShare, String investorOid, String productOid, BigDecimal totalIncome);
	
	
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set totalVolume = totalVolume - ?2, "
			+ " accruableHoldVolume = IF(accruableHoldVolume >= ?2, accruableHoldVolume - ?2, 0) , "
			+ " holdVolume = holdVolume - ?2, "
			+ " redeemableHoldVolume = redeemableHoldVolume - ?2, "
			+ " value = totalVolume * ?3"
			+ " where oid = ?1 "
			+ " and totalVolume >= ?2 and holdVolume >= ?2 and redeemableHoldVolume >= ?2", nativeQuery = true)
	@Modifying
	public int flatExpGoldVolume(String oid, BigDecimal orderVolume, BigDecimal netUnitShare);
	
	
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set totalVolume = totalVolume - ?1, "
			+ " maxHoldVolume = IF (maxHoldVolume >= ?1, maxHoldVolume - ?1, 0),"
			+ " accruableHoldVolume = IF(accruableHoldVolume >= ?1, accruableHoldVolume - ?1, 0) , "
			+ " holdVolume = holdVolume - ?1, "
			+ " redeemableHoldVolume = redeemableHoldVolume - ?1, value = totalVolume * ?2"
			+ " where investorOid = ?3 and productOid = ?4 "
			+ "and redeemableHoldVolume >= ?1 and totalVolume >= ?1 and holdVolume >= ?1", nativeQuery = true)
	@Modifying
	public int fastRedeem(BigDecimal volume,BigDecimal netUnitShare,String investorOid, String productOid);
	
	
	@Query(value = "update PublisherHoldEntity set totalVolume = totalVolume - ?1, "
			+ " toConfirmInvestVolume = toConfirmInvestVolume - ?1,"
			+ " totalInvestVolume = totalInvestVolume - ?1, "
			+ " value = value - ?1 where investorBaseAccount = ?2 and product = ?3 "
			+ " and totalVolume > ?1 and value >= ?1 and toConfirmInvestVolume >= ?1 ")
	@Modifying
	public int abandon4T0Invest(BigDecimal volume, InvestorBaseAccountEntity investorBaseAccount, Product product);
	
	
	@Query(value = "update PublisherHoldEntity set totalVolume = totalVolume - ?1, "
			+ " holdVolume = holdVolume - ?1,"
			+ " lockRedeemHoldVolume = lockRedeemHoldVolume - ?1,"
			+ " accruableHoldVolume = IF(accruableHoldVolume >= ?1, accruableHoldVolume - ?1, 0),"
			+ " totalInvestVolume = totalInvestVolume - ?1, "
			+ " value = value - ?1 where investorBaseAccount = ?2 and product = ?3 "
			+ " and totalVolume > ?1 and value >= ?1 and toConfirmInvestVolume >= ?1 ")
	@Modifying
	public int abandon4TnInvest(BigDecimal volume, InvestorBaseAccountEntity investorBaseAccount, Product product);
	
	/**
	 * 活期赎回锁定、还本锁定
	 * @param volume 锁定份额
	 * @param investorBaseAccount 投资人
	 * @param product 产品
	 * @return
	 */
//	@Query(value = "update PublisherHoldEntity set toConfirmRedeemVolume = toConfirmRedeemVolume + ?1, dayRedeemVolume = dayRedeemVolume + ?1, "
//			+ " redeemableHoldVolume = redeemableHoldVolume - ?1 where investorBaseAccount = ?2 and product = ?3 and redeemableHoldVolume - ?1 >= 0")
//	@Query(value = "update PublisherHoldEntity set dayRedeemVolume = dayRedeemVolume + ?1 "
//			+ " where investorBaseAccount = ?2 and product = ?3 ")
//	@Modifying
//	public int redeemLock(BigDecimal volume, InvestorBaseAccountEntity investorBaseAccount, Product product);
	
	@Query(value = "update PublisherHoldEntity set dayRedeemVolume = dayRedeemVolume + ?1 where investorBaseAccount = ?2 and product = ?3 and dayRedeemVolume + ?1 <= ?4")
	@Modifying
	public int redeem4DayRedeemVolume(BigDecimal orderVolume, InvestorBaseAccountEntity investorBaseAccount, Product product, BigDecimal singleDailyMaxRedeem);
	
	@Query(value = "update PublisherHoldEntity set dayRedeemVolume = dayRedeemVolume + ?1 where investorBaseAccount = ?2 and product = ?3")
	@Modifying
	public int redeem4DayRedeemVolumeSuperAccount(BigDecimal orderVolume, InvestorBaseAccountEntity investorBaseAccount, Product product);
	
	@Query(value = "update PublisherHoldEntity set toConfirmRedeemVolume = toConfirmRedeemVolume - ?1, redeemableHoldVolume = redeemableHoldVolume + ?1 "
			+ "where investorBaseAccount = ?2 and product = ?3 and toConfirmRedeemVolume - ?1 >= 0")
	@Modifying
	public int redeem4Refuse(BigDecimal volume, InvestorBaseAccountEntity investorBaseAccount, Product product);
	
	@Query(value = "update PublisherHoldEntity set dayRedeemVolume = dayRedeemVolume - ?1 "
			+ "where investorBaseAccount = ?2 and product = ?3 and dayRedeemVolume >= ?1")
	@Modifying
	public int redeem4RefuseOfDayRedeemVolume(BigDecimal orderVolume, InvestorBaseAccountEntity investorBaseAccount, Product product);
	
	
	@Query(value = "update PublisherHoldEntity set dayInvestVolume = dayInvestVolume - ?2 "
			+ " where oid = ?1 and dayInvestVolume >= ?2")
	@Modifying
	public int invest4AbandonOfDayInvestVolume(String holdOid, BigDecimal orderVolume);
	
	
	/**
	 * 获取指定产品下面的所有持有人名录
	 */
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where productOid = ?1 and holdStatus = 'holding' "
			+ " and oid > ?2 and accountType = 'INVESTOR' order by oid limit 1000", nativeQuery = true)
	public List<PublisherHoldEntity> findByProduct(String productOid, String lastOid);
	
	
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where productOid = ?1 and holdStatus = 'holding' and redeemableHoldVolume > 0 "
			+ " and oid > ?3 and accountType = ?2 order by oid limit 1000", nativeQuery = true)
	public List<PublisherHoldEntity> clearingHold(String productOid, String accountType, String lastOid);
	
	@Modifying
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set totalVolume = totalVolume + ?2 + ?4,"
			+ " redeemableHoldVolume = redeemableHoldVolume + ?2, "
			+ " lockRedeemHoldVolume = lockRedeemHoldVolume + ?4, "
			+ " holdVolume = holdVolume + ?2 + ?4, "
			+ " holdTotalIncome = holdTotalIncome + ?3 + ?5, holdYesterdayIncome = ?3 + ?5, "
			+ " accruableHoldVolume = accruableHoldVolume + ?2 + ?4, "
			+ " value = totalVolume * ?6, "
			+ " totalBaseIncome = totalBaseIncome + ?8, yesterdayBaseIncome = ?8,"
			+ " totalRewardIncome = totalRewardIncome + ?9, yesterdayRewardIncome = ?9, "
			+ " confirmDate = ?7 where oid = ?1", nativeQuery = true)
	public int updateHold4Interest(String holdOid, BigDecimal holdInterestVolume, BigDecimal holdInterestAmount, 
			BigDecimal holdLockIncomeVolume, BigDecimal holdLockIncomeAmount, BigDecimal netUnitAmount, 
			Date incomeDate, BigDecimal holdInterestBaseAmount, BigDecimal holdInterestRewardAmount);
	
//	@Modifying
//	@Query(value = "update T_MONEY_PUBLISHER_HOLD set value = value + ?2 + ?3, "
//			+ " totalVolume = totalVolume + ?2 + ?3, "
//			+ " holdVolume = holdVolume + ?2 + ?3, "
//			+ " holdTotalIncome = holdTotalIncome + ?2 + ?3, "
//			+ " incomeAmount = incomeAmount + ?2 + ?3,"
//			+ " confirmDate = ?4 where oid = ?1", nativeQuery = true)
	@Modifying
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set totalVolume = totalVolume + ?2 + ?3,"
			+ " lockRedeemHoldVolume = lockRedeemHoldVolume + ?3, "
			+ " holdVolume = holdVolume + ?2 + ?3, "
			+ " holdTotalIncome = holdTotalIncome + ?2 + ?3, holdYesterdayIncome = ?2 + ?3, "
			+ " accruableHoldVolume = accruableHoldVolume + ?2 + ?3, "
			+ " value = ?2 + ?3, "
			+ " totalBaseIncome = totalBaseIncome + ?2 + ?3, yesterdayBaseIncome = ?2 + ?3,"
			+ " confirmDate = ?4 where oid = ?1", nativeQuery = true)
	public int updateHold4InterestTn(String holdOid, BigDecimal holdIncomeAmount, BigDecimal holdLockIncomeAmount, Date incomeDate);

	
	
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where productOid = ?1 and holdStatus = ?2 and oid > ?3 and accountType = ?4 "
			+ " and (confirmDate < ?5 or confirmDate is null)order by oid limit 1000", nativeQuery = true)
	public List<PublisherHoldEntity> findByProductAndHoldStatus(String productOid, String holdStatus, String lastOid, String accountType, Date incomeDate);
	

	@Query("from PublisherHoldEntity e where e.assetPool = ?1 and e.publisherBaseAccount = ?2 and e.accountType='SPV'")
	public List<PublisherHoldEntity> findByAssetPoolEntityAndSPV(AssetPoolEntity assetPool, PublisherBaseAccountEntity spv);
	
	/**
	 * spv赎回订单审核确定调整totalVolume
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query(value = "update PublisherHoldEntity set totalVolume = totalVolume - ?2 where oid = ?1 and totalVolume-lockRedeemHoldVolume >= ?2")
	@Modifying
	public int spvOrderRedeemConfirm(String oid,BigDecimal orderAmount);
	
	/**
	 * spv申购订单审核确定调整totalVolume
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query(value = "update PublisherHoldEntity set totalVolume = totalVolume + ?2 where oid = ?1 ")
	@Modifying
	public int spvOrderInvestConfirm(String oid,BigDecimal orderAmount);
		
	/**
	 * 投资 增加SPV锁定赎回金额
	 */
	@Query("update PublisherHoldEntity set lockRedeemHoldVolume = lockRedeemHoldVolume + ?3  "
			+ " where product = ?1 and accountType = ?2 and totalVolume - lockRedeemHoldVolume >= ?3")
	@Modifying
	public int checkSpvHold4Invest(Product product, String accountType, BigDecimal orderVolume);
	
	/**
	 * 活期废单 
	 */
	@Query("update PublisherHoldEntity set lockRedeemHoldVolume = lockRedeemHoldVolume - ?2  where product = ?1 "
			+ " and accountType = 'SPV' and lockRedeemHoldVolume >= ?2 ")
	@Modifying
	public int updateSpvHold4T0InvestAbandon(Product product, BigDecimal orderVolume);
	
	/**
	 * 定期废单 
	 */
	@Query("update PublisherHoldEntity set totalVolume = totalVolume + ?3  where product = ?1 and accountType = 'SPV'")
	@Modifying
	public int updateSpvHold4TnInvestAbandon(Product product, BigDecimal orderVolume);
	

	/**
	 * 赎回份额确认
	 */
	@Query("update PublisherHoldEntity set totalVolume = totalVolume + ?3  where product = ?1 and accountType = ?2")
	@Modifying
	public int update4RedeemConfirm(Product product, String accountType, BigDecimal orderVolume);
	
	/**
	 * //更新SPV持仓
	 * @param product
	 * @param publisherAccounttypeSpv
	 * @param orderVolume
	 * @return
	 */
	@Query("update PublisherHoldEntity set lockRedeemHoldVolume = lockRedeemHoldVolume - ?3, totalVolume = totalVolume - ?3  where product = ?1 and accountType = ?2 and lockRedeemHoldVolume >= ?3")
	@Modifying
	public int update4InvestConfirm(Product product, String publisherAccounttypeSpv,
			BigDecimal orderVolume);
	
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where productOid = ?1 and accountType = 'INVESTOR' order by oid limit ?2, ?3", nativeQuery = true)
	public List<PublisherHoldEntity> queryHoldList(String productOid, int offset, int limit);
	
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where productOid = ?1 and investorOid = ?2", nativeQuery = true)
	public List<PublisherHoldEntity> queryHoldList(String productOid, String investorOid);
	
	@Query("update PublisherHoldEntity set updateTime = sysdate()  where product = ?1 and investorBaseAccount = ?2 and redeemableHoldVolume = ?3")
	@Modifying
	public int update4MinRedeem(Product product, InvestorBaseAccountEntity investorBaseAccount, BigDecimal orderAmount);
	
	@Query("update PublisherHoldEntity set maxHoldVolume = maxHoldVolume + ?4  where product = ?2 and investorBaseAccount = ?1 and maxHoldVolume + ?4 <= ?3")
	@Modifying
	public int checkMaxHold4Invest(InvestorBaseAccountEntity investorBaseAccount, Product product, BigDecimal proMaxHoldVolume, BigDecimal orderVolume);
	
	@Query("update PublisherHoldEntity set maxHoldVolume = maxHoldVolume - ?3  where product = ?2 and investorBaseAccount = ?1 and maxHoldVolume >= ?3")
	@Modifying
	public int updateMaxHold4InvestAbandon(InvestorBaseAccountEntity investorBaseAccount, Product product, BigDecimal orderVolume);
	
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set lockRedeemHoldVolume = lockRedeemHoldVolume + ?3, "
			+ " redeemableHoldVolume = redeemableHoldVolume + ?2,"
			+ " accruableHoldVolume = accruableHoldVolume + ?4,"
			+ " maxHoldVolume = maxHoldVolume + ?3, "
			+ " holdVolume = holdVolume + ?5,"
			+ " toConfirmInvestVolume = toConfirmInvestVolume - ?5, "
			+ " holdStatus = if(holdStatus = 'toConfirm', 'holding', holdStatus) "
			+ " where oid = ?1 and toConfirmInvestVolume >= ?5", nativeQuery = true)
	@Modifying
	public int updateHold4Confirm(String holdOid, BigDecimal redeemableHoldVolume,
			BigDecimal lockRedeemHoldVolume, BigDecimal accruableHoldVolume, BigDecimal orderVolume);
	
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set expGoldVolume = expGoldVolume + ?3, "
			+ " accruableHoldVolume = accruableHoldVolume + ?2, "
			+ " maxHoldVolume = maxHoldVolume + ?3, "
			+ " holdVolume = holdVolume + ?3,"
			+ " toConfirmInvestVolume = toConfirmInvestVolume - ?3, "
			+ " holdStatus = if(holdStatus = 'toConfirm', 'holding', holdStatus) "
			+ " where oid = ?1 and toConfirmInvestVolume >= ?3" , nativeQuery = true)
	@Modifying
	public int updateHold4ExpGoldConfirm(String holdOid, BigDecimal accruableHoldVolume, BigDecimal orderVolume);
	
	
	@Query("update PublisherHoldEntity set dayRedeemVolume = 0, dayInvestVolume = 0, dayRedeemCount = 0 ")
	@Modifying
	public int resetToday();
	
	@Query("update PublisherHoldEntity set dayRedeemVolume = 0, dayInvestVolume = 0, dayRedeemCount = 0 where oid = ?1")
	@Modifying
	public int resetToday(String holdOid);
	
	/**
	 * 付息
	 * @param interest 利息
	 * @param investorBaseAccount 投资人
	 * @param product 产品
	 * @return
	 */
	@Query(value = "update PublisherHoldEntity set lockIncome=lockIncome-?1,value = value - ?1, updateTime = sysdate() where investorBaseAccount = ?4 and product= ?5 and lockIncome-?1 >=0 and value - ?1 >=0")
	@Modifying
	public int repayInterest(BigDecimal interest,InvestorBaseAccountEntity investorBaseAccount, Product product);	

	

	
	/**
	 * 查询活期产品详情（）
	 * @param userOid：用户uid
	 * @param productOid:产品oid
	 * @return
	 */
	@Query(value = " SELECT A.holdTotalIncome, "//累计收益
			+ " A.holdYesterdayIncome, "//昨日收益
			+ " (A.redeemableHoldVolume + A.lockRedeemHoldVolume + A.toConfirmRedeemVolume + A.expGoldVolume) holdVolume, "//持有份额
			+ " A.productOid, "//
			+ " C.incomeCalcBasis, "//收益计算基础
			+ " C.expAror, "//预期年化收益率
			+ " C.expArorSec, "//年化收益率区间
			+ " C.assetPoolOid, "//所属资产池
			+ " C.minRredeem, "//单笔赎回最低下限
			+ " C.maxRredeem, "//单笔赎回追加金额
			+ " C.additionalRredeem, "//单笔赎回最高份额
			+ " C.netUnitShare, "//单位净值
			+ " A.dayRedeemVolume, "//今日赎回金额
			+ " C.singleDailyMaxRedeem, "//单人单日赎回上限
			+ " C.dailyNetMaxRredeem, "//剩余赎回金额
			+ " C.netMaxRredeemDay, "//单日净赎回上限
			+ " A.redeemableHoldVolume, "//可赎回份额
			+ " C.rredeemDateType, "//赎回确认日期类型
			+ " C.redeemConfirmDays, "//赎回确认天数
			+ " C.productLabel "  // 产品标签
			+ " FROM T_MONEY_INVESTOR_BASEACCOUNT B "//
			+ " INNER JOIN T_MONEY_PUBLISHER_HOLD A ON A.investorOid = B.oid "//
			+ " INNER JOIN T_GAM_PRODUCT C ON A.productOid = C.oid "//
			+ " WHERE B.oid=?1 AND A.productOid=?2 ", nativeQuery = true)
	public List<Object[]> findProductDetail(String userOid, String productOid);
	
	/**
	 * 查询我的持有中定期产品列表（产品ID，产品名称，预期年化收益率，投资金额，到期日）
	 * */
	@Query(value = "SELECT A.oid,A.name,A.expAror,(B.redeemableHoldVolume+B.lockRedeemHoldVolume)*A.netUnitShare holdAmount,"
			+ " A.durationPeriodEndDate,B.holdStatus,B.toConfirmRedeemVolume*A.netUnitShare "
			+ " FROM T_MONEY_INVESTOR_BASEACCOUNT C "
			+ " INNER JOIN T_MONEY_PUBLISHER_HOLD B  ON B.investorOid = C.oid  "
			+ " INNER JOIN T_GAM_PRODUCT A ON A.oid = B.productOid "
			+ " WHERE C.userOid=?1 AND B.holdStatus IN (" + PublisherHoldEntity.STATUS_HOLD_MYPRODUCT + ") "
			+ " AND A.TYPE='PRODUCTTYPE_01' AND A.STATE in (" + PublisherHoldEntity.T1STATUS_HOLD_MYPRODUCT + ")  "
			+ " ORDER BY B.createTime DESC ", nativeQuery = true)
	public List<Object[]> myHoldregular(String userOid);
	
	/**
	 * 查询我的申请中定期产品列表（产品ID，产品名称，申请金额）
	 * */
	@Query(value = "SELECT A.oid,A.name,B.toConfirmInvestVolume*A.netUnitShare toConfirmAmount,"
			+ "(B.redeemableHoldVolume+B.lockRedeemHoldVolume)*A.netUnitShare holdAmount,B.toConfirmRedeemVolume*A.netUnitShare,B.holdStatus"
			+ " FROM T_MONEY_INVESTOR_BASEACCOUNT C "
			+ " INNER JOIN T_MONEY_PUBLISHER_HOLD B  ON B.investorOid = C.oid  "
			+ " INNER JOIN T_GAM_PRODUCT A ON A.oid = B.productOid "
			+ " WHERE C.userOid=?1 AND B.holdStatus IN (" + PublisherHoldEntity.STATUS_TOCONFIRM_MYPRODUCT + ") "
			+ " AND A.TYPE='PRODUCTTYPE_01' AND A.STATE IN (" + PublisherHoldEntity.T1STATUS_TOCONFIRM_MYPRODUCT + ") "
			+ " and totalVolume > 0 "
			+ " ORDER BY B.createTime DESC ", nativeQuery = true)
	public List<Object[]> myApplyregular(String userOid);
	
	/**
	 * 查询我的已结算定期产品列表
	 * */
	@Query(value = "SELECT A.oid,A.name,totalInvestVolume*A.netUnitShare holdAmount,A.setupDate,A.repayDate,"
			+ " SUBSTR(B.updateTime,1,10),B.holdStatus,B.holdTotalIncome "
			+ " FROM T_MONEY_INVESTOR_BASEACCOUNT C "
			+ " INNER JOIN T_MONEY_PUBLISHER_HOLD B  ON B.investorOid = C.oid  "
			+ " INNER JOIN T_GAM_PRODUCT A ON A.oid = B.productOid "
			+ " WHERE C.userOid=?1 AND B.holdStatus IN (" + PublisherHoldEntity.STATUS_CLOSED_MYPRODUCT + ") "
			+ " AND A.TYPE='PRODUCTTYPE_01' "
			+ " ORDER BY B.createTime DESC ", nativeQuery = true)
	public List<Object[]> myClosedregular(String userOid);
	
	/**
	 * 查询我的持有中定期产品详情（投资金额，预计收益率，预计收益，还本付息日）
	 * */
	@Query(value = "SELECT (B.redeemableHoldVolume+B.lockRedeemHoldVolume)*A.netUnitShare holdAmount,A.expAror,B.expectIncome,A.repayDate "
			+ " FROM T_MONEY_INVESTOR_BASEACCOUNT C "
			+ " INNER JOIN T_MONEY_PUBLISHER_HOLD B  ON B.investorOid = C.oid  "
			+ " INNER JOIN T_GAM_PRODUCT A ON A.oid = B.productOid "
			+ " WHERE C.userOid=?1 AND B.productOid=?2 ", nativeQuery = true)
	public List<Object[]> myHoldregularDetail(String userOid,String productOid);
	
	@Query(value = "select count(*) from T_MONEY_PUBLISHER_HOLD where investorOid = ?1 ", nativeQuery = true)
	public int queryTotalInvestProductsByinvestorBaseAccount(String investorOid);
	
	/**
	 * 获取指定产品下面的所有持有人名录
	 */
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where productOid = ?1 and holdStatus = 'holding' "
			+ " and oid > ?3 and accountType = ?2 order by oid limit 1000", nativeQuery = true)
	public List<PublisherHoldEntity> findByProductPaged(String productOid, String accountType, String lastOid);
	
	/**
	 * 募集成立或失败之前，募集期收益划入本金
	 */
	@Query(value = "update PublisherHoldEntity set "
			+ " accruableHoldVolume = accruableHoldVolume + lockIncome, "
			+ " lockRedeemHoldVolume = lockRedeemHoldVolume + lockIncome, lockIncome = 0, incomeAmount = 0 where product = ?1")
	@Modifying
	public int changeIncomeIntoHoldVolume(Product product);
	
	@Query(value = "update PublisherHoldEntity set redeemableHoldVolume = lockRedeemHoldVolume, lockRedeemHoldVolume = 0 where product = ?1")
	@Modifying
	public int unlockCash(Product product);
	
	@Query(value = "update PublisherHoldEntity set redeemableHoldVolume = lockRedeemHoldVolume, lockRedeemHoldVolume = 0, "
			+ " redeemableIncome = lockIncome, lockIncome = 0 where product = ?1")
	@Modifying
	public int unlockCashAndIncome(Product product);
	
	/**发行人下投资人质量分析（某个投资金额范围内的投资人个数）*/
	@Query(value = " SELECT A1.LEVEL,COUNT(*) FROM ( "
			+" SELECT A.investorOid, "
			+" CASE "
			+ "	  WHEN SUM(A.totalInvestVolume * B.netUnitShare) <=50000 THEN 1 "
			+"    WHEN SUM(A.totalInvestVolume * B.netUnitShare) BETWEEN 50000 AND 100000 THEN 2  "
			+"    WHEN SUM(A.totalInvestVolume * B.netUnitShare) BETWEEN 100000 AND 200000 THEN 3 "
			+"    ELSE 4 END LEVEL "
			+" FROM "
			+"   T_MONEY_PUBLISHER_HOLD A  "
			+"   INNER JOIN T_GAM_PRODUCT B ON A.productOid = B.oid  "
			+" WHERE A.publisherOid = ?1  "
			+" GROUP BY A.investorOid  "
			+" )A1 "
			+ " GROUP BY A1.LEVEL ASC ", nativeQuery = true)
	public List<Object[]> analyseInvestor(String publisherOid);
	
	public PublisherHoldEntity findByInvestorBaseAccount(InvestorBaseAccountEntity investorBaseAccount);
	
	@Query(value = "select count(*) from T_MONEY_PUBLISHER_HOLD where investorOid = ?1", nativeQuery = true)
	public int countByInvestorBaseAccount(String investorOid);
	
	@Query(value = "select count(*) from T_MONEY_PUBLISHER_HOLD where investorOid = ?1 and publisherOid = ?2", nativeQuery = true)
	public int countByPublisherBaseAccountAndInvestorBaseAccount(String investorOid, String publisherOid);
	
	/**平台下-投资人质量分析（某个投资金额范围内的投资人个数）*/
	@Query(value = " SELECT A1.LEVEL,COUNT(*) FROM ( "
			+" SELECT A.investorOid, "
			+" CASE "
			+ "	  WHEN SUM(A.totalInvestVolume * B.netUnitShare) <=50000 THEN 1 "
			+"    WHEN SUM(A.totalInvestVolume * B.netUnitShare) BETWEEN 50000 AND 100000 THEN 2  "
			+"    WHEN SUM(A.totalInvestVolume * B.netUnitShare) BETWEEN 100000 AND 200000 THEN 3 "
			+"    ELSE 4 END LEVEL "
			+" FROM "
			+"   T_MONEY_PUBLISHER_HOLD A  "
			+"   INNER JOIN T_GAM_PRODUCT B ON A.productOid = B.oid  "
			+" GROUP BY A.investorOid  "
			+" )A1 "
			+ " GROUP BY A1.LEVEL ASC ", nativeQuery = true)
	public List<Object[]> analysePlatformInvestor();
	
	
	@Query("from PublisherHoldEntity e where e.product = ?1 and e.accountType='SPV'")
	public List<PublisherHoldEntity> findSpvHoldByProduct(Product product);
	
	@Query(value = "select maxHoldVolume from T_MONEY_PUBLISHER_HOLD where investorOid = ?1 and productOid = ?2", nativeQuery = true)
	public BigDecimal findMaxHoldVol(String investorOid, String productOid);
	
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where investorOid = ?1 and holdStatus in ('holding', 'toConfirm', 'closing', 'refunding') ", nativeQuery = true)
	public List<PublisherHoldEntity> findByInvestorOid(String investorOid);
	

	@Query(value = "select t1.* from T_MONEY_PUBLISHER_HOLD t1, T_GAM_PRODUCT t2 where t1.investorOid = ?1 and t1.productOid = t2.oid and t2.type = 'PRODUCTTYPE_02' ", nativeQuery = true)
	public List<PublisherHoldEntity> queryMyT0HoldProList(String investorOid);
	
	@Query(value = "select t1.* from T_MONEY_PUBLISHER_HOLD t1, T_GAM_PRODUCT t2 where t1.investorOid = ?1 and t1.productOid = t2.oid and t2.type = 'PRODUCTTYPE_01' ", nativeQuery = true)
	public List<PublisherHoldEntity> queryMyTnHoldProList(String investorOid);
	

	@Query(value = "SELECT * FROM T_MONEY_PUBLISHER_HOLD "
			+ " WHERE accountType='INVESTOR' and oid > ?1 order by oid limit 2000", nativeQuery = true)
	public List<PublisherHoldEntity> getHoldByBatch(String lastOid);
	
	@Query(value = "SELECT a.productOid,b.userOid,a.toConfirmRedeemVolume,"
			+ "a.redeemableHoldVolume,a.totalInvestVolume,a.dayRedeemVolume,"
			+ "a.dayInvestVolume,a.maxHoldVolume,a.lockRedeemHoldVolume,a.accountType,"
			+ "a.holdVolume,a.totalVolume,a.accruableHoldVolume,a.toConfirmInvestVolume,"
			+ "a.holdStatus,a.value,a.oid, a.expGoldVolume "
			+ "FROM T_MONEY_PUBLISHER_HOLD a LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT b "
			+ "ON a.investorOid=b.oid "
			+ "WHERE a.accountType='INVESTOR' "
			+ "AND b.userOid=?1", nativeQuery = true)
	public List<Object[]> getHoldByUserOid(String UserOid);
	
	@Query(value = "SELECT * FROM `T_MONEY_PUBLISHER_HOLD` WHERE accountType='SPV'", nativeQuery = true)
	public List<PublisherHoldEntity> getSPVHold();
	
	@Query(value = "SELECT productOid,publisherOid,toConfirmRedeemVolume,redeemableHoldVolume,"
			+ " totalInvestVolume,dayRedeemVolume,dayInvestVolume,maxHoldVolume,lockRedeemHoldVolume,totalVolume,oid "
			+ " FROM `T_MONEY_PUBLISHER_HOLD` WHERE accountType='SPV' AND productOid=?1", nativeQuery = true)
	public List<Object[]> getSPVHoldByProductOid(String productOid);
	/**
	 * 获取含有体验金的用户
	 */
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where oid > ?1 and expGoldVolume > 0 order by oid limit 2000 ", nativeQuery = true)
	public List<PublisherHoldEntity> getAllExpHolds(String lastOid);
	
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where oid > ?1 and accountType = 'INVESTOR' and (dayRedeemCount > 0 or dayInvestVolume > 0)  order by oid limit 2000 ", nativeQuery = true)
	public List<PublisherHoldEntity> getResetTodayHold(String lastOid);
	
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set dayRedeemCount = dayRedeemCount + 1 where dayRedeemCount + 1 <= ?1 and productOid=?2 and investorOid=?3",nativeQuery = true)
	@Modifying
	public int updateDayRedeemCount(Integer singleDayRedeemCount,String productOid,String investorOid);
	
	@Query(value = "update T_MONEY_PUBLISHER_HOLD set dayRedeemCount = dayRedeemCount + 1 where productOid=?1 and investorOid=?2",nativeQuery = true)
	@Modifying
	public int updateDayRedeemCountSuperAccount(String productOid,String investorOid);
	
	@Query(value = "select * from T_MONEY_PUBLISHER_HOLD where oid > ?1 and productOid = '0e4ee8ead74241de90f82e06f987ef8d' and accountType != 'SPV' and createTime < '2017-02-22 00:00:01' order by oid limit 2000 ", nativeQuery = true)
	public List<PublisherHoldEntity> dealHold(String lastOid);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getTnHoldList
	 * @Description: 我的定期列表查询
	 * @param uid
	 * @param holdStatus
	 * @param tnStartDate
	 * @param tnEndDate
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年9月8日 下午2:32:21
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " mit.orderCode orderOid, gp.oid productOid, gp.name productName,IF(gp.guessOid IS NULL, 0, 1) relatedGuess, gp.state productStatus, mit.orderStatus orderStatus, "
			+ " mit.holdStatus holdStatus, gp.durationPeriodDays durationPeriodDays, mit.payAmount payAmount, mit.couponAmount couponAmount, CONCAT(TRUNCATE(IFNULL(mitc.interest,0), 2), '%') addInterest, "
			+ " mit.orderAmount orderAmount, CONCAT(TRUNCATE(gp.expAror*100, 2), '%') expAror, mit.expectIncome expectIncome, CONCAT(TRUNCATE(IF(gai.ratio IS NULL,gp.expAror*100, gai.ratio), 2) ,'%') realRatio, "
			+ " DATE_FORMAT(mit.orderTime,'%Y-%m-%d %H:%i:%s') investTime, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') payDate, gp.setupDate setupDate, "
			+ " gp.durationPeriodEndDate durationPeriodEndDate, gp.repayDate repayDate, DATE_FORMAT(gp.clearedTime,'%Y-%m-%d') realCashDate, gp.raiseFailDate raiseFailDate, gp.instruction instruction, TRUNCATE(mit.totalIncome,2) realIncome "
			+ ", CONCAT(TRUNCATE(gp.expectedArrorDisp*100, 2) ,'%') expectedArrorDisp,pd.url activityDetailUrl,mitc.interestDays,mit.couponType,gp.incomeCalcBasis,gp.isActivityProduct, gp.isP2PAssetPackage"
			+ " FROM t_money_investor_tradeorder mit "
			+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
			+ " LEFT JOIN t_gam_product_detail pd ON pd.oid = gp.activityDetail "
			+ " LEFT JOIN t_money_investor_tradeorder_coupon mitc ON mit.oid = mitc.orderOid "
			+ " LEFT JOIN t_gam_allocate_interest_audit gai ON gp.oid = gai.productOid AND gai.auditStatus = 'AUDITPASS' "
			+ " WHERE gp.type = 'PRODUCTTYPE_01' AND gp.isP2PAssetPackage=0  AND mit.holdStatus IS NOT NULL "
			+ " AND mit.investorOid = ?1 "
			+ " AND ( "
			+ "     CASE WHEN ?2=0 THEN mit.holdStatus IN ('holding', 'closed', 'refunded') "
			+ "          WHEN ?2=1 THEN mit.holdStatus IN ('holding') "
			+ "          WHEN ?2=2 THEN mit.holdStatus IN ('closed', 'refunded') "
			+ "     END "
			+ " ) "
			+ " AND IF(?3 IS NULL OR ?3 = '', 1=1, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') >= ?3) "
			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') <= ?4) "
			+ " ORDER BY mit.orderTime DESC LIMIT ?5, ?6 ", nativeQuery = true)
	public List<Object[]> getTnHoldList(String uid, int holdStatus, String tnStartDate, String tnEndDate, int pageRow, int row);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getTnHoldCount
	 * @Description: 我的定期总条数查询
	 * @param uid
	 * @param holdStatus
	 * @param tnStartDate
	 * @param tnEndDate
	 * @return int
	 * @date 2017年9月8日 下午2:32:52
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " COUNT(1) "
			+ " FROM t_money_investor_tradeorder mit "
			+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
			+ " LEFT JOIN t_money_investor_tradeorder_coupon mitc ON mit.oid = mitc.orderOid "
			+ " LEFT JOIN t_gam_allocate_interest_audit gai ON gp.oid = gai.productOid AND gai.auditStatus = 'AUDITPASS' "
			+ " WHERE gp.type = 'PRODUCTTYPE_01' AND mit.holdStatus IS NOT NULL "
			+ " AND mit.investorOid = ?1 "
			+ " AND ( "
			+ "     CASE WHEN ?2=0 THEN mit.holdStatus IN ('holding', 'closed', 'refunded') "
			+ "          WHEN ?2=1 THEN mit.holdStatus IN ('holding') "
			+ "          WHEN ?2=2 THEN mit.holdStatus IN ('closed', 'refunded') "
			+ "     END "
			+ " ) "
			+ " AND IF(?3 IS NULL OR ?3 = '', 1=1, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') >= ?3) "
			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') <= ?4) ", nativeQuery = true)
	public int getTnHoldCount(String uid, int holdStatus, String tnStartDate, String tnEndDate);

	@Query(value = "SELECT oid,productOid,investorOid,TRUNCATE(totalVolume*10000, 0),TRUNCATE(holdVolume*10000, 0),TRUNCATE(toConfirmInvestVolume*10000, 0), "
			+ "TRUNCATE(toConfirmRedeemVolume*10000, 0),TRUNCATE(totalInvestVolume*10000, 0),TRUNCATE(lockRedeemHoldVolume*10000, 0), "
			+ "TRUNCATE(redeemableHoldVolume*10000, 0),TRUNCATE(accruableHoldVolume*10000, 0),TRUNCATE(VALUE*10000, 0),TRUNCATE(expGoldVolume * 10000, 0), "
			+ "TRUNCATE(holdTotalIncome * 10000, 0),TRUNCATE(holdYesterdayIncome * 10000, 0),confirmDate,TRUNCATE(expectIncomeExt * 10000, 0), "
			+ "TRUNCATE(expectIncome * 10000, 0),TRUNCATE(dayRedeemVolume*10000, 0),TRUNCATE(dayInvestVolume*10000, 0),dayRedeemCount, "
			+ "TRUNCATE(maxHoldVolume*10000, 0),holdStatus,latestOrderTime from T_MONEY_PUBLISHER_HOLD "
			+ "where investorOid = ?1 and productOid = ?2", nativeQuery = true)
	public Object[] findHoldByInvestorAndProduct(String investorOid, String productOid);

	/**
	 *
	 *日切按照holdoid查找持仓
	 * @author yujianlong
	 * @date 2018/4/5 12:00
	 * @param [holdOidList, productOid]
	 * @return java.util.List<com.guohuai.mmp.publisher.hold.PublisherHoldEntity>
	 */
	@Query(value="SELECT * FROM t_money_publisher_hold a where a.oid in (?1) and a.redeemableHoldVolume > 0 and a.productOid =?2 and a.holdStatus = 'holding' ",nativeQuery=true)
	List<PublisherHoldEntity> getHoldsIn(List<String> holdOidList,String productOid);



	@Query(value = " SELECT a.*  FROM T_MONEY_PUBLISHER_HOLD a, t_money_investor_tradeorder b, t_money_investor_opencycle_tradeorder_relation c  " +
            " WHERE a.oid = b.holdOid AND b.orderCode = c.sourceOrderCode AND a.productOid = ?1 AND a.holdStatus = 'holding' " +
            " AND a.holdVolume > 0 AND a.oid > ?3 AND a.accountType = 'INVESTOR' AND b.orderType IN ('changeInvest', 'continueInvest') " +
            " AND b.orderStatus = 'confirmed' AND c.continueStatus = ?2 " +
            " ORDER BY a.oid  LIMIT 1000 ", nativeQuery = true)
	List<PublisherHoldEntity> findByProductOidAndContinueStatus(String productOid, int continueStatus, String lastOid);

	@Query(value = "UPDATE t_money_publisher_hold SET holdVolume = 0, redeemableHoldVolume = 0, accruableHoldVolume = 0, holdStatus = 'close' WHERE oid IN (?1)", nativeQuery = true)
	@Modifying
	int updateHold4ContinueInvest(List<String> holdOids);

    @Query(value = " SELECT * FROM T_MONEY_PUBLISHER_HOLD WHERE investorOid = ?1 AND productOid = ?2", nativeQuery = true)
    PublisherHoldEntity findByInvestorAndProduct(String investorOid, String productOid);

    @Query(value = " SELECT * FROM T_MONEY_PUBLISHER_HOLD WHERE assetpoolOid = ?1 AND productOid = ?2 AND accountType='SPV'; ", nativeQuery = true)
	PublisherHoldEntity findByAssetPoolOidAndProductOid(String assetPoolOid, String productOid);

	/**
	 *
	 * @author jiangjianmin
	 * @Title: getScatterHoldList
	 * @Description: 我的企业散标列表查询
	 * @param uid
	 * @param holdStatus
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年9月8日 下午2:32:21
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " mit.oid orderOid, gp.oid productOid, gp.name productName,IF(gp.guessOid IS NULL, 0, 1) relatedGuess, gp.state productStatus, mit.orderStatus orderStatus, "
			+ " mit.holdStatus holdStatus, gp.durationPeriodDays durationPeriodDays, mit.payAmount payAmount, mit.couponAmount couponAmount, CONCAT(TRUNCATE(IFNULL(mitc.interest,0), 2), '%') addInterest, "
			+ " mit.orderAmount orderAmount, CONCAT(TRUNCATE(gp.expAror*100, 2), '%') expAror, mit.expectIncome expectIncome, CONCAT(TRUNCATE(IF(gai.ratio IS NULL,gp.expAror*100, gai.ratio), 2) ,'%') realRatio, "
			+ " DATE_FORMAT(mit.orderTime,'%Y-%m-%d %H:%i:%s') investTime, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') payDate, gp.setupDate setupDate, "
			+ " gp.durationPeriodEndDate durationPeriodEndDate, gp.repayDate repayDate, DATE_FORMAT(gp.clearedTime,'%Y-%m-%d') realCashDate, gp.raiseFailDate raiseFailDate, gp.instruction instruction, TRUNCATE(mit.totalIncome,2) realIncome "
			+ ", CONCAT(TRUNCATE(gp.expectedArrorDisp*100, 2) ,'%') expectedArrorDisp,pd.url activityDetailUrl,mitc.interestDays,mit.couponType,gp.incomeCalcBasis,gp.isActivityProduct, gp.isP2PAssetPackage"
			+ " FROM t_money_investor_tradeorder mit "
			+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
			+ " LEFT JOIN t_gam_product_detail pd ON pd.oid = gp.activityDetail "
			+ " LEFT JOIN t_money_investor_tradeorder_coupon mitc ON mit.oid = mitc.orderOid "
			+ " LEFT JOIN t_gam_allocate_interest_audit gai ON gp.oid = gai.productOid AND gai.auditStatus = 'AUDITPASS' "
			+ " WHERE gp.type = 'PRODUCTTYPE_01' AND gp.isP2PAssetPackage=2  AND mit.holdStatus IS NOT NULL "
			+ " AND mit.investorOid = ?1 "
			+ " AND ( "
			+ "     CASE WHEN ?2=0 THEN mit.holdStatus IN ('holding', 'closed', 'refunded') "
			+ "          WHEN ?2=1 THEN mit.holdStatus IN ('holding') "
			+ "          WHEN ?2=2 THEN mit.holdStatus IN ('closed', 'refunded') "
			+ "     END "
			+ " ) "
//			+ " AND IF(?3 IS NULL OR ?3 = '', 1=1, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') >= ?3) "
//			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') <= ?4) "
			+ " ORDER BY mit.orderTime DESC LIMIT ?3, ?4 ", nativeQuery = true)
	public List<Object[]> getScatterHoldList(String uid, int holdStatus, int pageRow, int row);

	/**
	 *
	 * @author jiangjianmin
	 * @Title: getScatterHoldCount
	 * @Description: 我的企业散标定期总条数查询
	 * @param uid
	 * @param holdStatus
	 * @return int
	 * @date 2017年9月8日 下午2:32:52
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " COUNT(1) "
			+ " FROM t_money_investor_tradeorder mit "
			+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
			+ " LEFT JOIN t_money_investor_tradeorder_coupon mitc ON mit.oid = mitc.orderOid "
			+ " LEFT JOIN t_gam_allocate_interest_audit gai ON gp.oid = gai.productOid AND gai.auditStatus = 'AUDITPASS' "
			+ " WHERE gp.type = 'PRODUCTTYPE_01' AND gp.isP2PAssetPackage=2 AND mit.holdStatus IS NOT NULL "
			+ " AND mit.investorOid = ?1 "
			+ " AND ( "
			+ "     CASE WHEN ?2=0 THEN mit.holdStatus IN ('holding', 'closed', 'refunded') "
			+ "          WHEN ?2=1 THEN mit.holdStatus IN ('holding') "
			+ "          WHEN ?2=2 THEN mit.holdStatus IN ('closed', 'refunded') "
			+ "     END "
			+ " ) "
//			+ " AND IF(?3 IS NULL OR ?3 = '', 1=1, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') >= ?3) "
//			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1, DATE_FORMAT(mit.orderTime,'%Y-%m-%d') <= ?4) "
			, nativeQuery = true)
	public int getScatterHoldCount(String uid, int holdStatus);
}
