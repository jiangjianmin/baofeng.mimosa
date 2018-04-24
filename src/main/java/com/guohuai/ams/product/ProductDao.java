package com.guohuai.ams.product;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.guess.GuessEntity;
import com.guohuai.ams.product.productChannel.ProductChannel;
import com.guohuai.ams.productPackage.ProductPackage;

public interface ProductDao extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

	/**
	 * 根据产品编号获取产品实体
	 * 
	 * @param productCode
	 * @return {@link Product}
	 */
	public Product findByCode(String code);

	public List<Product> findByOidIn(List<String> oids);

	public List<Product> findByState(String state);

	/**
	 *
	 *获取03产品未删除的数量,用于新建循环产品
	 * @author yujianlong
	 * @date 2018/3/20 17:24
	 * @param []
	 * @return int
	 */
	@Query(value = " SELECT count(1) FROM t_gam_product WHERE type='PRODUCTTYPE_03' AND isDeleted = 'NO' ", nativeQuery = true)
	public int getProduct03Count();

	/**
	 *
	 *获取审核通过的03产品，用于定时任务日切判断
	 * @author yujianlong
	 * @date 2018/3/21 16:57
	 * @param []
	 * @return int
	 */
	@Query(value = " SELECT t1.* FROM t_gam_product t1,T_GAM_PRODUCT_CHANNEL t2 WHERE t1.oid = t2.productOid AND t1.type = 'PRODUCTTYPE_03' AND t2.marketState = 'ONSHELF' AND t1.isDeleted = 'NO' ", nativeQuery = true)
	public List<Product> getProduct03ReviewList();

	/**
	 *
	 *获取持有人手册中03产品可赎回份额大于0的所有持仓数量，用于日切
	 * @author yujianlong
	 * @date 2018/3/21 17:15
	 * @param []
	 * @return int
	 */
	@Query(value =" select count(1) from t_money_publisher_hold a  where a.productOid=?1 "
			, nativeQuery = true)
	public int getPublisherHoldProduct03Count(String productOid);


	/**
	 * SPV订单审核确定调整 募集总份额(产品可售头寸)
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query("update Product set raisedTotalNumber = raisedTotalNumber+?2, updateTime = sysdate() where oid = ?1 ")
	@Modifying
	public int adjustRaisedTotalNumber(String oid, BigDecimal orderAmount);

	/**
	 * 废单：解锁产品锁定已募份额 
	 */
	@Query("update Product set lockCollectedVolume = lockCollectedVolume - ?2  where oid = ?1 and lockCollectedVolume >= ?2")
	@Modifying
	public int update4InvestAbandon(String productOid, BigDecimal orderVolume);
	
	/**
	 * 投资减少可售份额，增加锁定金额
	 * @param oid
	 * @param orderVolume
	 * @return
	 * 活期废单：解锁产品锁定已募份额 
	 */
	@Query("update Product set lockCollectedVolume = lockCollectedVolume - ?2  where oid = ?1 and lockCollectedVolume >= ?2")
	@Modifying
	public int update4T0InvestAbandon(String productOid, BigDecimal orderVolume);
	
	/**
	 * 定期废单 
	 */
	@Query("update Product set currentVolume = currentVolume - ?2, collectedVolume = collectedVolume - ?2, maxSaleVolume = maxSaleVolume + ?2"
			+ "  where oid = ?1 and currentVolume >= ?2 and collectedVolume >= ?2 ")
	@Modifying
	public int update4TnInvestAbandon(String productOid, BigDecimal orderVolume);
	
	
	
	/**
	 * 投资减少可售份额，增加锁定金额
	 */
	@Query("update Product set lockCollectedVolume = lockCollectedVolume + ?2  where oid = ?1 and maxSaleVolume - lockCollectedVolume >= ?2")
	@Modifying
	public int update4Invest(String oid, BigDecimal orderVolume);
	
	/**
	 * 份额确认之后解除锁定份额
	 */
	@Query("update Product set lockCollectedVolume = lockCollectedVolume - ?2, maxSaleVolume = maxSaleVolume - ?2, "
			+ "currentVolume = currentVolume + ?2, collectedVolume = collectedVolume + ?2  where oid = ?1  and maxSaleVolume >= ?2 and lockCollectedVolume >= ?2")
	@Modifying
	public int update4InvestConfirm(String productOid, BigDecimal orderVolume);
	
	/**
	 * 减少单日赎回份额
	 * @param oid
	 * @param orderVolume
	 * @return
	 */
	@Query("update Product set dailyNetMaxRredeem = dailyNetMaxRredeem - ?2  where oid = ?1 and dailyNetMaxRredeem >= ?2")
	@Modifying
	public int update4Redeem(String productOid, BigDecimal orderVolume);
	
	@Query("update Product set dailyNetMaxRredeem = dailyNetMaxRredeem + ?2  where oid = ?1")
	@Modifying
	public int update4RedeemRefuse(String productOid, BigDecimal orderVolume);
	
	/**
	 * 赎回确认
	 * @param oid
	 * @param orderVolume
	 * @return
	 */
	@Query("update Product set  currentVolume = currentVolume - ?2  where oid = ?1 and currentVolume >= ?2")
	@Modifying
	public int update4RedeemConfirm(String productOid, BigDecimal orderVolume);
	
	@Query(value = "update Product set purchaseNum = purchaseNum + 1 where oid = ?1")
	@Modifying
	public int updatePurchaseNum(String productOid);
	
	@Query(value = "update Product set purchasePeopleNum = purchasePeopleNum + 1, purchaseNum = purchaseNum + 1 where oid = ?1")
	@Modifying
	public int updatePurchasePeopleNumAndPurchaseNum(String productOid);
	
	/**
	 * 资产池收益分配成功发送更新产品currentVolume
	 * @param oid
	 * @param incomeAllocateSuccesAmount
	 * @return
	 */
	@Query("update Product set currentVolume = currentVolume+?2, basicRatio = ?3, updateTime = sysdate() where oid = ?1 ")
	@Modifying
	public int incomeAllocateAdjustCurrentVolume(String oid, BigDecimal incomeAllocateSuccesAmount, BigDecimal basicRatio);
	
	@Query("update Product set state = 'CLEARED', clearedTime = sysdate() where oid = ?1 and currentVolume = 0 ")
	@Modifying
	public int update4Liquidation(String oid);
	
	/**
	 * 清结算之前 需发放收益活期产品
	 */
	@Query(value = "select * from T_GAM_PRODUCT where and spvOid = ?1 and  state in ('DURATIONING', 'CLEARING') and type ='PRODUCTTYPE_02' ", nativeQuery = true)
	public List<Product> needIncomeBeforeOffset(String spvOid);
	
	/**
	 * 活期产品 存续期、清盘中 发放收益
	 */
	@Query(value = "select * from T_GAM_PRODUCT where state in ('DURATIONING', 'CLEARING') and type ='PRODUCTTYPE_02' ", nativeQuery = true)
	public List<Product> findProductT04Snapshot();
	
	/**
	 *定期产品 募集期 发放收益
	 */
	@Query(value = "select * from T_GAM_PRODUCT where state in ('RAISING') and recPeriodExpAnYield > 0 and type ='PRODUCTTYPE_01' ", nativeQuery = true)
	public List<Product> findProductTn4Snapshot();
	
	@Query(value = "select * from T_GAM_PRODUCT where state in ('RAISING', 'RAISEEND') and recPeriodExpAnYield > 0 and type ='PRODUCTTYPE_01' ", nativeQuery = true)
	public List<Product> findProductTn4Interest();

	/**
	 * 快定宝产品新建轧差批次
	 */
	@Query(value = "select * from T_GAM_PRODUCT where state = 'RAISING' and type ='PRODUCTTYPE_03' AND isDeleted = 'NO'", nativeQuery = true)
	Product getBfPlusProduct();

	/**
	 * 活期产品新建轧差批次
	 */
	@Query(value = "select * from T_GAM_PRODUCT where spvOid = ?1 and state in ('DURATIONING', 'CLEARING') and type ='PRODUCTTYPE_02' ", nativeQuery = true)
	public List<Product> findProductT04NewOffset(String spvOid);
	/**
	 * 定期产品新建轧差批次
	 */
	@Query(value = "select * from T_GAM_PRODUCT where spvOid = ?1 and state in ('RAISING', 'RAISEEND') and type ='PRODUCTTYPE_01' ", nativeQuery = true)
	public List<Product> findProductTn4NewOffset(String spvOid);
	/**
	 * 快定宝产品新建轧差批次
	 */
	@Query(value = "select * from T_GAM_PRODUCT where spvOid = ?1 and state in ('RAISING', 'RAISEEND') and type ='PRODUCTTYPE_03' ", nativeQuery = true)
	List<Product> findBfPlusNewOffset(String spvOid);

	@Query(value = "from Product where isDeleted = 'NO' and state != 'CLEARED' ")
	public List<Product> findByProduct4Contract();
	
	@Query("from Product where repayLoanStatus = 'toRepay' and repayDate >= ?1")
	public List<Product> getRepayLoanProduct(Date repayDate);
	
	@Query("from Product where repayInterestStatus = 'toRepay' and repayDate >= ?1")
	public List<Product> getRepayInterestProduct(Date repayDate);
	
	@Query("update Product set fastRedeemLeft = fastRedeemLeft - ?2 where oid = ?1 and fastRedeemLeft >= ?2 ")
	@Modifying
	public int updateFastRedeemLeft(String productOid, BigDecimal orderVolume);
	
	@Query("update Product set fastRedeemLeft = fastRedeemMax where fastRedeemStatus = 'YES' and state != 'CLEARED' and isDeleted = 'NO' ")
	@Modifying
	public int resetFastRedeemLeft();
	
	@Query(value = "update Product set fastRedeemStatus = ?3,fastRedeemLeft = ?2 + fastRedeemLeft - fastRedeemMax, fastRedeemMax = ?2,operator = ?4,updateTime = sysdate()  where oid = ?1 and fastRedeemMax-fastRedeemLeft <= ?2")
	@Modifying
	public int updateFastRedeemMax(String oid,BigDecimal fastRedeemMax,String fastRedeemStatus,String operator);
	
	
	@Query(value = "update Product set repayLoanStatus = ?2, repayInterestStatus = ?3  where oid = ?1")
	@Modifying
	public int updateRepayStatus(String oid, String repayLoanStatus, String repayInterestStatus);
	
	@Query(value = "from Product where state ='Durationend' and repayLoanStatus = 'toRepay' and repayDate < ?1 and overdueStatus != 'yes' ")
	public List<Product> getOverdueProduct(Date curDate);
	
	@Query(value = "update Product set repayLoanStatus = 'repaying', repayInterestStatus = 'repaying' "
			+ " where oid = ?1 and repayLoanStatus in ('toRepay', 'repayFailed') and repayInterestStatus in ('toRepay', 'repayFailed') ")
	@Modifying
	public int repayLock(String productOid);
	
	/**
	 * 可售份额申请生效
	 * @param oid
	 * @param applyAmount 申请份额
	 * @param holdTotalVolume 发行人持有份额
	 * @return
	 */
	@Query("update Product set maxSaleVolume = maxSaleVolume + ?2  where oid = ?1  and ?3 - maxSaleVolume >= ?2 ")
	@Modifying
	public int updateMaxSaleVolume(String productOid, BigDecimal applyAmount, BigDecimal holdTotalVolume);
	
	/**
	 * 查询有标签的产品所有列表
	 * @param channeOid
	 * @param labelCode
	 * @param nowDate
	 * @param offset
	 * @param rows
	 * @return
	 */
	@Query(value = "SELECT p.oid productOid,p.incomeCalcBasis,d.oid type,p.expAror,p.expArorSec,c.oid channelOid,p.code productCode,p.name productName,p.fullName productFullName, "
			+"p.currentVolume currentVolume,p.collectedVolume collectedVolume,p.lockCollectedVolume lockCollectedVolume,p.investMin investMin,p.lockPeriodDays lockPeriodDays, "
			+"p.durationPeriodDays durationPeriod,p.raisedTotalNumber raisedTotalNumber,p.maxSaleVolume maxSaleVolume,p.state state,p.netUnitShare netUnitShare,p.purchaseNum purchaseNum "
			+ " FROM T_GAM_PRODUCT p "
			+ " INNER JOIN T_GAM_PRODUCT_CHANNEL pc ON pc.productOid = p.oid AND pc.marketState = '"+ProductChannel.MARKET_STATE_Onshelf+"'"
			+ " INNER JOIN T_MONEY_PLATFORM_CHANNEL c ON c.oid = pc.channelOid AND c.oid = ?1 AND c.channelStatus = '"+Channel.CHANNEL_STATUS_ON+"' AND c.deleteStatus = '"+Channel.CHANNEL_DELESTATUS_NO+"'"
			+ " INNER JOIN T_GAM_DICT d ON d.oid = p.type "
			+ " INNER JOIN T_MONEY_PLATFORM_LABEL_PRODUCT pl ON pl.productOid = p.oid "
			+ " INNER JOIN T_MONEY_PLATFORM_LABEL l ON l.oid = pl.labelOid AND l.labelCode = ?2 "
			+ " WHERE p.isDeleted = '"+Product.NO+"' AND p.isOpenPurchase = '"+Product.YES+"'"
			+" AND ((d.oid = '"+Product.TYPE_Producttype_02+"' AND p.state = '"+Product.STATE_Durationing+"' AND p.setupDate <= ?3 ) "
			+" OR (d.oid = '"+Product.TYPE_Producttype_01+"' AND p.state = '"+Product.STATE_Raising+"' AND p.raiseStartDate <= ?3 AND p.raiseEndDate >= ?3 )) "
			+ " ORDER BY pc.rackTime DESC ", nativeQuery = true)
	public List<Object[]> queryLabelProducts(String channeOid, String labelCode, Date nowDate);
	
	
	/**
	 * 获取在售中的产品
	 * @return
	 */
	@Query(value = "from Product WHERE state in ('RAISING','DURATIONING')")
	public List<Product> findOnSaleProducts();
	
	@Query(value = "select * from T_GAM_PRODUCT where type = 'PRODUCTTYPE_02' and state = 'DURATIONING' ", nativeQuery = true)
	public List<Product> getOnSaleProductOid();
	
	@Query(value = "SELECT distinct a.* FROM T_GAM_PRODUCT a LEFT JOIN T_GAM_INCOME_REWARD b ON a.oid=b.productOid LEFT JOIN T_MONEY_PLATFORM_LABEL_PRODUCT c ON a.`oid`=c.productOid WHERE a.type = 'PRODUCTTYPE_02' AND a.state = 'DURATIONING' AND b.oid IS NULL", nativeQuery = true)
	public List<Product> getOnSaleNoAwardDemandProductOid();
	
	/**
	 * 活期产品
	 */
	@Query(value = "select * from T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, "
			+ " T_MONEY_PLATFORM_CHANNEL t3, T_MONEY_PLATFORM_LABEL_PRODUCT t4,  T_MONEY_PLATFORM_LABEL t5 where t1.oid = t2.productOid and "
			+ " t2.channelOid = t3.oid and t2.channelOid = ?1 and t2.marketState = 'ONSHELF' and t4.labelOid = t5.oid and t4.productOid = t1.oid and t5.labelCode != 'experienceFund' "
			+ " and t1.type = 'PRODUCTTYPE_02' and t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') limit 1", nativeQuery = true)
	public Product getT0Product(String channelOid);
	
	/**
	 * 活期产品列表
	 */
	@Query(value = "select distinct t1.* from T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, "
			+ " T_MONEY_PLATFORM_CHANNEL t3, T_MONEY_PLATFORM_LABEL_PRODUCT t4,  T_MONEY_PLATFORM_LABEL t5 where t1.oid = t2.productOid and "
			+ " t2.channelOid = t3.oid and t2.channelOid = ?1 and t2.marketState = 'ONSHELF' and t4.labelOid = t5.oid and t4.productOid = t1.oid and t5.labelCode != 'experienceFund' "
			+ " and t1.type = 'PRODUCTTYPE_02' and t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') order by t1.createTime desc", nativeQuery = true)
	public List<Product> getT0ProductList(String channelOid);

	/**
	 * 快定宝产品列表
	 * @param channelOid	渠道号
	 * @return	产品集合
	 */
	@Query(value = "select distinct t1.* from T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, "
			+ " T_MONEY_PLATFORM_CHANNEL t3, T_MONEY_PLATFORM_LABEL_PRODUCT t4 where t1.oid = t2.productOid and "
			+ " t2.channelOid = t3.oid and t2.channelOid = ?1 and t2.marketState = 'ONSHELF' and t4.productOid = t1.oid "
			+ " and t1.type = 'PRODUCTTYPE_03' and t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') order by t1.createTime desc", nativeQuery = true)
	List<Product> getBfPlusProductList(String channelOid);

	/**
	 * 查询产品利率最大的一个散标产品
	 * @author yihonglei
	 * @date 2018/4/21 14:06
	 * @param channelOid 渠道号
	 * @return Product 产品对象
	 * @throws 
	 * @since 1.0.0
	 */
	@Query(value = "select distinct t1.* from T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, "
			+ " T_MONEY_PLATFORM_CHANNEL t3, T_MONEY_PLATFORM_LABEL_PRODUCT t4 where t1.oid = t2.productOid and "
			+ " t2.channelOid = t3.oid and t2.channelOid = ?1 and t2.marketState = 'ONSHELF' and t4.productOid = t1.oid "
			+ " and t1.type = 'PRODUCTTYPE_01' and t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') "
			+ " AND isP2PAssetPackage = 2 order by t1.expAror desc LIMIT 1 ", nativeQuery = true)
	Product getScatterProduct(String channelOid);

	/**
	 *
	 *获取可以募集满额生成电子签章的企业散标oid
	 *
	 * @author yujianlong
	 * @date 2018/4/23 16:27
	 * @param []
	 * @return java.util.List<java.lang.String>
	 */
	@Query(value = "select distinct t1.oid from T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, "
			+ " T_MONEY_PLATFORM_CHANNEL t3, T_MONEY_PLATFORM_LABEL_PRODUCT t4 where t1.oid = t2.productOid and "
			+ " t2.channelOid = t3.oid  and t2.marketState = 'ONSHELF' and t4.productOid = t1.oid "
			+ " and t1.type = 'PRODUCTTYPE_01' and t1.state in ('RAISING', 'RAISEEND', 'DURATIONING', 'DURATIONEND') "
			+ " AND isP2PAssetPackage = 2  ", nativeQuery = true)
	List<String> getScatter2DurationingProductOids();




	/**
	 * 新手标
	 */
//	@Query(value = "SELECT t1.* FROM T_GAM_PRODUCT t1 , T_MONEY_PLATFORM_LABEL_PRODUCT t2, T_MONEY_PLATFORM_LABEL t3, T_GAM_PRODUCT_CHANNEL t4, "
//			+ " T_MONEY_PLATFORM_CHANNEL t5 WHERE t1.oid = t2.productOid " 
//			+  " AND t2.labelOid = t3.oid AND t4.productOid = t1.oid AND t4.channelOid = t5.oid AND t5.oid = ?1 AND t4.marketState = 'ONSHELF' "
//			+ " AND t3.labelCode = 'freshman' AND t1.state in ('RAISING') order by t4.rackTime LIMIT 1", nativeQuery = true)
	@Query(value = "SELECT t1.* FROM T_GAM_PRODUCT t1 , T_MONEY_PLATFORM_LABEL_PRODUCT t2, T_MONEY_PLATFORM_LABEL t3, T_GAM_PRODUCT_CHANNEL t4, "
			+ " T_MONEY_PLATFORM_CHANNEL t5 WHERE t1.oid = t2.productOid " 
			+  " AND t2.labelOid = t3.oid AND t4.productOid = t1.oid AND t4.channelOid = t5.oid AND t5.oid = ?1 AND t4.marketState = 'ONSHELF' "
			+ " AND t3.labelCode = 'freshman' AND t1.state in ('RAISING') AND (t1.maxSaleVolume - IFNULL(t1.investMin,0)) >=0 order by t4.rackTime desc LIMIT 1", nativeQuery = true)
	public Product getNewBieProduct(String channelOid);

	/**
	 * author yujianlong
	 * 需求禅道号355
	 * @param channelOid
	 * @return
	 */
	@Query(value = "SELECT t1.* FROM T_GAM_PRODUCT t1 , T_MONEY_PLATFORM_LABEL_PRODUCT t2, T_MONEY_PLATFORM_LABEL t3, T_GAM_PRODUCT_CHANNEL t4, "
			+ " T_MONEY_PLATFORM_CHANNEL t5 WHERE t1.oid = t2.productOid " 
			+  " AND t2.labelOid = t3.oid AND t4.productOid = t1.oid AND t4.channelOid = t5.oid AND t5.oid = ?1 AND t4.marketState = 'ONSHELF' "
			+ " AND t3.labelCode = 'freshman' AND t1.state in ('RAISING') AND t1.maxSaleVolume >=0 "
			+" AND t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume >0 "
//			+" AND t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume >=IFNULL(t1.investMin,0) "
			+ " order by t1.expAror desc,t1.expArorSec desc,t4.rackTime asc LIMIT 1 ", nativeQuery = true)
	public Product getNewBieProduct4App(String channelOid);

	
	@Query("update Product set auditState = 'Locking'  where oid = ?1 and  auditState = 'REVIEWED' ")
	@Modifying
	public int lockProduct(String productOid);
	
	
	@Query("update Product set auditState = 'REVIEWED'  where oid = ?1 and  auditState = 'Locking' ")
	@Modifying
	public int unLockProduct(String productOid);
	
	@Query(value = "update Product set repayInterestStatus = 'repayed', interestAuditStatus = 'AUDITPASS' "
			+ "  where oid = ?1 and  repayInterestStatus = 'repaying' and interestAuditStatus = 'INTERESTING' ")
	@Modifying
	public int repayInterestOk(String productOid);
	
	@Query(value = "update Product set repayInterestStatus = 'repaying' "
			+ " where oid = ?1 and repayInterestStatus in ('toRepay', 'repayFailed') ")
	@Modifying
	public int repayInterestLock(String productOid);
	
	@Query(value = "update Product set repayLoanStatus = 'repaying' "
			+ " where oid = ?1 and repayLoanStatus in ('toRepay', 'repayFailed') and  repayInterestStatus = 'repayed' ")
	@Modifying
	public int repayLoanLock(String productOid);
	
	@Query(value = "update Product set repayLoanStatus = ?2, interestAuditStatus = ?3 "
			+ " where oid = ?1 and repayLoanStatus in ('repaying') and  repayInterestStatus = 'repayed' and interestAuditStatus = 'REPAYING' ")
	@Modifying
	public int repayLoanEnd(String productOid, String repayLoanStatus, String interestAuditStatus);
	
	@Query(value = 
			  " SELECT orderTime,realName,phoneNum,orderAmount FROM ( "
			  + "  SELECT "
			  + "     DATE_FORMAT(mit.orderTime,'%Y-%m-%d %H:%i:%s') orderTime, "
			  + "     CASE WHEN LENGTH(mib.realName) <= 2 THEN CONCAT(SUBSTR(mib.realName,1,1),'**') ELSE CONCAT(SUBSTR(mib.realName,1,1),'*',SUBSTR(mib.realName,3,1)) END realName, "
			  + "     CONCAT(SUBSTR(mib.phoneNum,1,3),'****',SUBSTR(mib.phoneNum,8,4)) phoneNum, "
			  + "     TRUNCATE(mit.orderAmount,2) orderAmount "
			  + "  FROM t_money_investor_tradeorder mit "
			  + "  LEFT JOIN t_money_investor_baseaccount mib ON mit.investorOid = mib.oid "
			  + "  WHERE mit.orderStatus = 'confirmed' "
			  + "  AND mit.productOid = ?1 "
			  + "  UNION ALL "
			  + "  SELECT "
			  + "     DATE_FORMAT(mso.fictitiousTime,'%Y-%m-%d %H:%i:%s') orderTime, "
			  + "     '陈*翔' realName, "
			  + "     '187****7474' phoneNum, "
			  + "     TRUNCATE(mso.orderAmount,2) orderAmount "
			  + "  FROM t_money_supplement_order mso "
			  + "  WHERE mso.orderStatus = 'created' "
			  + "  AND mso.productOid = ?1 "
			  + " ) proInvInfo ORDER BY orderTime DESC "
			  + " LIMIT ?2,?3 ", nativeQuery=true)
	public List<Object[]> queryProductInvestRecordAll(String productOid, int i, int row);
	
	@Query(value = 
			  " SELECT COUNT(1) FROM ( "
			  + "  SELECT "
			  + "     DATE_FORMAT(mit.orderTime,'%Y-%m-%d %H:%i:%s') orderTime, "
			  + "     CASE WHEN LENGTH(mib.realName) <= 2 THEN CONCAT(SUBSTR(mib.realName,1,1),'**') ELSE CONCAT(SUBSTR(mib.realName,1,1),'*',SUBSTR(mib.realName,3,1)) END realName, "
			  + "     CONCAT(SUBSTR(mib.phoneNum,1,3),'****',SUBSTR(mib.phoneNum,8,4)) phoneNum, "
			  + "     TRUNCATE(mit.orderAmount,2) orderAmount "
			  + "  FROM t_money_investor_tradeorder mit "
			  + "  LEFT JOIN t_money_investor_baseaccount mib ON mit.investorOid = mib.oid "
			  + "  WHERE mit.orderStatus = 'confirmed' "
			  + "  AND mit.productOid = ?1 "
			  + "  UNION ALL "
			  + "  SELECT "
			  + "     DATE_FORMAT(mso.fictitiousTime,'%Y-%m-%d %H:%i:%s') orderTime, "
			  + "     '陈*翔' realName, "
			  + "     '187****7474' phoneNum, "
			  + "     TRUNCATE(mso.orderAmount,2) orderAmount "
			  + "  FROM t_money_supplement_order mso "
			  + "  WHERE mso.orderStatus = 'created' "
			  + "  AND mso.productOid = ?1 "
			  + " ) proInvInfo ", nativeQuery=true)
	public int queryProductInvestRecordCount(String productOid);
	
	@Query(value="SELECT p.oid,p.name FROM t_gam_product p LEFT JOIN t_money_platform_label_product p_l  ON(p.oid=p_l.`productOid`) LEFT JOIN t_money_platform_label l ON(l.`oid`=p_l.`labelOid`) WHERE p.`type`='PRODUCTTYPE_01' AND l.`labelCode` != 'freshman'",nativeQuery = true)
	public List<Object[]>  findTnProductAndNotFreashman();
	
	@Query(value = " FROM Product WHERE type='PRODUCTTYPE_02' AND state IN ('DURATIONING','DURATIONEND','CLEARING','CLEARED') ")
	public List<Product> getT0ProductProfit();
	/** 根据需求--写死，写死，写死！ */
	@Query(value = " SELECT * FROM t_gam_product WHERE type='PRODUCTTYPE_02' AND isDeleted = 'NO' AND state IN ('DURATIONING') AND NAME = '快活宝' ", nativeQuery = true)
	public List<Product> getT0ProductAbleInvest();

	@Query(value = "from Product where packageOid = ?1 ")
	public List<Product> productFromProductPackage(String packageOid);
	
	@Query(value = "from Product where packageOid = ?1 and state = 'RAISING' ")
	public List<Product> raisingProductFromProductPackage(String packageOid);
	
	/**
	 * 获取资产池下的产品（不包含募集失败的）
	 * @return
	 */
	@Query(value = "from Product WHERE assetPoolOid = ?1 and isDeleted = 'NO' and state != 'RAISEFAIL' ")
	public List<Product> findProducts(String assetPoolOid);
	
	/**
	 * 根据产品id查询产品是否可以用于红包
	 */
	@Query(value = "select * from T_GAM_PRODUCT where oid=?1 and state = 'RAISING' and type ='PRODUCTTYPE_01' and productLabel!='freshman' and guessOid is null ", nativeQuery = true)
	public List<Product> findRedPackProduct(String oid);

	/**根据竞猜活动查产品
	 * @param guess
	 * @return
	 */
	@Query("from Product where guess = ?1")
	public List<Product> findByGuess(GuessEntity guess);

	/**根据产品包查询产品列表
	 * @param productPackage
	 * @return
	 */
	@Query("from Product where productPackage = ?1")
	public List<Product> findByPackage(ProductPackage productPackage);
	
	@Query(value = "select p.* from T_GAM_GUESS g inner join T_GAM_PRODUCT p on(g.oid = p.guessOid) "
			+ " where g.oid = ?1 order by p.createTime desc limit 1" ,nativeQuery= true)
	public Product findCurrentProductByGuessOid(String guessOid);
	
	/**
	* <p>Title:查询该产品是否关联竞猜宝 ，返回不为NULL的话，说明关联竞猜宝</p>
	* <p>Description: </p>
	* <p>Company: </p> 
	* @param productOid
	* @return
	* @author 邱亮
	* @date 2017年7月24日 下午5:36:18
	* @since 1.0.0
	*/
	@Query(value = "select * from T_GAM_PRODUCT where oid = ?1 and guessOid is not null" ,nativeQuery= true)
	public Product findProductByOidAndGuessOidIsNotNull(String productOid);
	

	/**
	 * @Desc: 根据产品id查询存续期
	 * @author huyong
	 * @date 2017.10.18
	 */
	@Query(value="select  t1.durationPeriodDays from T_GAM_PRODUCT t1 where  t1.oid = ?1", nativeQuery=true)
	public int findDurationPeriodDaysByOid(String oid);


	@Query("update Product set interestAuditStatus = ?3 where oid = ?1 and interestAuditStatus = ?2 ")
	@Modifying
	public int updateInterestAuditStatus(String productOid, String interestAuditStatusFrom, String interestAuditStatusTo);

	/**
	 * 循环产品修改产品状态、派息状态
	 * @return
	 */
	@Modifying
	@Query(value = " UPDATE t_gam_product a " +
			" SET a.state             = 'DURATIONEND', " +
			"  a.interestAuditStatus = 'AUDITPASS', " +
			"  a.repayInterestStatus = 'repaying' " +
			" WHERE a.type = 'PRODUCTTYPE_04' " +
			"      AND a.state = 'DURATIONING' " +
			"      AND a.repayInterestStatus = 'toRepay' " +
			"      AND a.repayLoanStatus = 'toRepay' " +
			"      AND a.durationPeriodEndDate <= subdate(current_date, 1) ", nativeQuery = true)
	int cycleProductInterestLock();

	/**
	 * 循环产品修改派息状态
	 * @return
	 */
	@Modifying
	@Query(value = " UPDATE t_gam_product a " +
			" SET a.repayInterestStatus = 'repayed' " +
			" WHERE a.type = 'PRODUCTTYPE_04' " +
			"      AND a.repayInterestStatus = 'repaying'", nativeQuery = true)
	int cycleProductInterestDone();


	@Modifying
	@Query(value = " UPDATE t_gam_product a, t_money_investor_tradeorder b, t_money_publisher_hold c " +
			" SET c.totalVolume          = c.totalVolume + b.expectIncome, " +
			"  c.holdVolume           = c.holdVolume + b.expectIncome, " +
			"  c.redeemableHoldVolume = c.redeemableHoldVolume + b.expectIncome, " +
			"  c.accruableHoldVolume  = c.accruableHoldVolume + b.expectIncome, " +
			"  c.holdTotalIncome      = c.holdTotalIncome + b.expectIncome, " +
			"  c.totalBaseIncome      = c.totalBaseIncome + b.expectIncome, " +
			"  c.yesterdayBaseIncome  = b.expectIncome, " +
			"  c.holdYesterdayIncome  = b.expectIncome, " +
			"  b.incomeAmount         = b.expectIncome, " +
			"  c.value                = c.value + b.expectIncome, " +
			"  c.confirmDate          = current_date(), " +
			"  b.holdVolume           = b.holdVolume + b.expectIncome, " +
			"  b.totalIncome          = b.totalIncome + b.expectIncome, " +
			"  b.totalBaseIncome      = b.totalBaseIncome + b.expectIncome, " +
			"  b.yesterdayBaseIncome  = b.expectIncome, " +
			"  b.yesterdayIncome      = b.expectIncome, " +
			"  b.incomeAmount         = b.expectIncome, " +
			"  b.VALUE                = b.VALUE + b.expectIncome, " +
			"  b.confirmDate          = current_date() " +
			" WHERE a.oid = b.productOid AND b.holdOid = c.oid " +
			"      AND a.type = 'PRODUCTTYPE_04' " +
			"      AND a.repayInterestStatus = 'repaying' " +
			"      AND a.repayLoanStatus = 'toRepay' " +
			"      AND b.orderType IN ('changeInvest', 'continueInvest') AND b.orderStatus = 'confirmed' ", nativeQuery = true)
	public int cycleProductInterest();

	@Modifying
	@Query(value = " INSERT IGNORE INTO T_MONEY_CYCLE_PRODUCT_OPERATING_LIST(orderCode, investorOid, holdOid, operateDate, orderAmount) " +
			"  SELECT " +
			"    b.orderCode, " +
			"    b.investorOid, " +
			"    c.oid, " +
			"    current_date(), " +
			"    c.holdVolume " +
			"  FROM t_gam_product a, t_money_investor_tradeorder b, t_money_publisher_hold c, " +
			"    t_money_investor_opencycle_tradeorder_relation d " +
			"  WHERE a.oid = b.productOid AND b.holdOid = c.oid AND b.orderCode = d.sourceOrderCode " +
			"        AND a.type = 'PRODUCTTYPE_04' " +
			"        AND a.state = 'DURATIONEND' " +
			"        AND a.repayInterestStatus = 'repayed' " +
			"        AND a.repayLoanStatus = 'toRepay' " +
			"		 AND c.holdVolume > 0 " +
			"        AND b.orderType IN ('changeInvest', 'continueInvest') AND b.orderStatus = 'confirmed' " +
			"        AND d.continueStatus = 1 ", nativeQuery = true)
	public int cycleProductAddToOperatingList();

	@Modifying
	@Query(value = " DELETE FROM t_money_cycle_product_operating_list WHERE status = 1 ", nativeQuery = true)
	public int deleteToRepayListNoUseData();

	@Query(value = " SELECT * " +
			" FROM t_gam_product a " +
			" WHERE a.type = 'PRODUCTTYPE_04' AND a.state = 'DURATIONEND' " +
			"      AND a.repayInterestStatus = 'repayed' " +
			"      AND a.repayLoanStatus IN ('toRepay', 'repayFailed') ", nativeQuery = true)
	List<Product> getDurationEndCycleProductList();

	@Query(value = " UPDATE t_gam_product a, (SELECT " +
			"                           b.oid                  AS productOid, " +
			"                           sum(c.totalBaseIncome) AS income " +
			"                         FROM t_gam_product b, t_money_publisher_hold c " +
			"                         WHERE b.oid = c.productOid " +
			"                               AND b.type = 'PRODUCTTYPE_04' AND b.repayInterestStatus = 'repaying' AND " +
			"                               b.repayLoanStatus = 'toRepay') d " +
			" SET a.currentVolume = a.currentVolume + d.income " +
			" WHERE a.oid = d.productOid", nativeQuery = true)
	@Modifying
	int distributeCycleProductInterestToCurrentVolume();

	@Query(value = " SELECT " +
			"  t1.oid, " +
			"  t1.name, " +
			"  t1.investMin, " +
			"  t1.expAror, " +
			"  t1.expArorSec, " +
			"  t1.rewardInterest, " +
			"  t1.durationPeriodDays, " +
			"  t1.raisedTotalNumber, " +
			"  t1.collectedVolume, " +
			"  t1.lockCollectedVolume, " +
			"  CASE WHEN t1.state = 'RAISING' " +
			"    THEN 1 " +
			"  WHEN t1.state = 'RAISEEND' " +
			"    THEN 2 " +
			"  WHEN t1.state = 'DURATIONING' " +
			"    THEN 3 " +
			"  WHEN t1.state = 'DURATIONEND' " +
			"    THEN 4 " +
			"  WHEN t1.state = 'CLEARED' " +
			"    THEN 5 " +
			"  ELSE 6 END AS                         stateOrder, " +
			"  t1.state, " +
			"  t1.type, " +
			"  CASE WHEN t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume = 0 " +
			"    THEN 1 " +
			"  ELSE 0 END AS                         investableVolume, " +
			"  t1.purchaseNum, " +
			"  DATE_FORMAT(t1.setupDate, '%Y-%m-%d') setupDate " +
			" FROM T_GAM_PRODUCT t1, T_GAM_PRODUCT_CHANNEL t2, T_MONEY_PLATFORM_CHANNEL t3 " +
			" WHERE t1.oid = t2.productOid AND t2.channelOid = t3.oid AND t1.type = 'PRODUCTTYPE_01' AND t2.marketState = 'ONSHELF' " +
			"      AND t3.oid = ?1 " +
			"      AND t1.guessOid IS NULL " +
			"      AND t1.productLabel = ?2 " +
			"      AND t1.isActivityProduct = 0 " +
			" ORDER BY stateOrder, investableVolume, t2.rackTime DESC " +
			" LIMIT 15", nativeQuery = true)
    List<Object[]> findProductsByChannelAndProductLabel(String channelOid, String label);

	@Query(value = " SELECT * FROM t_gam_product WHERE assetPoolOid = ?1 AND type = 'PRODUCTTYPE_03' AND isDeleted = 'NO' ", nativeQuery = true)
    Product getType03ProductByAssetPoolOid(String assetPoolOid);
}
