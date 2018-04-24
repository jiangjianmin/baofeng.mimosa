package com.guohuai.mmp.platform.baseaccount.statistics;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountEntity;

public interface PlatformStatisticsDao extends JpaRepository<PlatformStatisticsEntity, String>, JpaSpecificationExecutor<PlatformStatisticsEntity> {

	PlatformStatisticsEntity findByPlatformBaseAccount(PlatformBaseAccountEntity baseAccount);
	
	@Query(value=" SELECT ROUND(totalLoanAmount/100000000,0) totalLoanAmount,registerAmount FROM t_money_platform_statistics LIMIT 1 ",nativeQuery = true)
	public List<Object[]> queryHomeStat();
	
	/**
	 * 投资人充值回调
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query(value = "update PlatformStatisticsEntity set totalTradeAmount = totalTradeAmount + ?2, "
			+ "investorTotalDepositAmount = investorTotalDepositAmount + ?2 where oid = ?1")
	@Modifying
	int updateStatistics4InvestorDeposit(String oid, BigDecimal orderAmount);
	
	/**
	 * 发行人充值回调
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query(value = "update PlatformStatisticsEntity set totalTradeAmount = totalTradeAmount + ?2, "
			+ "publisherTotalDepositAmount = publisherTotalDepositAmount + ?2 where oid = ?1")
	@Modifying
	int updateStatistics4PublisherDeposit(String oid, BigDecimal orderAmount);
	
	/**
	 * 投资人提现回调
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query(value = "update PlatformStatisticsEntity set totalTradeAmount = totalTradeAmount + ?2, "
			+ "investorTotalWithdrawAmount = investorTotalWithdrawAmount + ?2 where oid = ?1")
	@Modifying
	int updateStatistics4InvestorWithdraw(String oid, BigDecimal orderAmount);
	
	/**
	 * 发行人提现回调
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query(value = "update PlatformStatisticsEntity set totalTradeAmount = totalTradeAmount + ?2, "
			+ "publisherTotalWithdrawAmount = publisherTotalWithdrawAmount + ?2 where oid = ?1")
	@Modifying
	int updateStatistics4PublisherWithdraw(String oid, BigDecimal orderAmount);
	
	/**
	 * 用户注册
	 * 增加平台注册人数
	 */
	@Query(value = "update PlatformStatisticsEntity set registerAmount = registerAmount + 1 where oid = ?1")
	@Modifying
	int increaseRegisterAmount(String oid);
	
	/**
	 * 投资人实名认证
	 * 增加平台实名人数
	 */
	@Query(value = "update PlatformStatisticsEntity set verifiedInvestorAmount = verifiedInvestorAmount + 1 where oid = ?1")
	@Modifying
	int increaseVerifiedInvestorAmount(String oid);
	
	/**
	 * 投资单份额确认:累计交易额、累计借款额
	 */
	@Query(value = "update PlatformStatisticsEntity set totalTradeAmount = totalTradeAmount + ?2, "
			+ " totalLoanAmount = totalLoanAmount + ?2 where oid = ?1")
	@Modifying
	int updateStatistics4InvestConfirm(String oid, BigDecimal orderAmount);
	
	/**
	 * 赎回单份额确认:累计交易额、累计还款额
	 */
	@Query(value = "update PlatformStatisticsEntity set totalTradeAmount = totalTradeAmount + ?2, "
			+ " totalReturnAmount = totalReturnAmount + ?2 where oid = ?1")
	@Modifying
	int updateStatistics4RedeemConfirm(String oid, BigDecimal orderAmount);
	
	/**
	 * 累计付息总额
	 */
	@Query(value = "update PlatformStatisticsEntity set totalTradeAmount = totalTradeAmount + ?2, "
			+ " totalInterestAmount = totalInterestAmount + ?2 where oid = ?1")
	@Modifying
	int updateStatistics4TotalInterestAmount(String oid, BigDecimal income);
	
	
	/**
	 * 投资人数
	 * 更新投资人数、持仓人数
	 */
	@Query(value = "update PlatformStatisticsEntity set investorAmount = investorAmount + 1, investorHoldAmount = investorHoldAmount + 1  where oid = ?1")
	@Modifying
	int increaseInvestorAmount(String oid);
	
	/**
	 * 快赎确认
	 */
	@Query(value = "update PlatformStatisticsEntity set totalTradeAmount = totalTradeAmount + ?2  where oid = ?1")
	@Modifying
	int updateStatistics4FastRedeemConfirm(String oid, BigDecimal orderAmount);
	
	
	/**
	 * 发行产品数
	 */
	@Query("UPDATE PlatformStatisticsEntity  SET productAmount = productAmount + 1 WHERE oid = ?1")
	@Modifying
	public int increaseReleasedProductAmount(String oid);
	
	/**
	 * 在售
	 */
	@Query("UPDATE PlatformStatisticsEntity  SET onSaleProductAmount = onSaleProductAmount + 1 WHERE oid = ?1")
	@Modifying
	public int increaseOnSaleProductAmount(String oid);
	
	/**
	 * 待结算
	 */
	@Query("UPDATE PlatformStatisticsEntity  SET onSaleProductAmount = onSaleProductAmount - 1, "
			+ "toCloseProductAmount = toCloseProductAmount + 1 WHERE oid = ?1")
	@Modifying
	public int increaseToCloseProductAmount(String oid);
	
	/**
	 * 已结算
	 */
	@Query("UPDATE PlatformStatisticsEntity  SET toCloseProductAmount = toCloseProductAmount - 1, "
			+ "closedProductAmount = closedProductAmount + 1 WHERE oid = ?1")
	@Modifying
	public int increaseClosedProductAmount(String oid);

	/**平台首页统计--平台统计信息*/
	@Query(value=" SELECT confirmDate,"//#统计日期
			+" totalTradeAmount, "//#累计交易总额
			+" totalLoanAmount, "//#累计借款总额
			+" registerAmount, "//#累计注册人数
			+" investorAmount, "//#累计投资人数
			+" investorTotalDepositAmount, "//#投资人累计充值总额
			+" investorTotalWithdrawAmount, "//#投资人累计提现总额
			+" totalReturnAmount, "//#累计还款总额
			+" publisherAmount, "//#企业用户数
			+" productAmount, "//#发行产品数
			+" closedProductAmount, "//#已还产品数
			+" onSaleProductAmount, " //#今日在售产品数
			+" activeInvestorAmount, "//#当日在线用户数
			+" verifiedInvestorAmount "//#累计实名认证用户数  
			+" FROM ( "
			+"  SELECT ?2 confirmDate,totalTradeAmount,totalLoanAmount,registerAmount,investorAmount,investorTotalDepositAmount,investorTotalWithdrawAmount,totalReturnAmount,publisherAmount,productAmount,closedProductAmount,onSaleProductAmount,activeInvestorAmount,verifiedInvestorAmount "
			+"  FROM T_MONEY_PLATFORM_STATISTICS A "
			+"  UNION ALL "
			+"  SELECT confirmDate,totalTradeAmount,totalLoanAmount,registerAmount,investorAmount,investorTotalDepositAmount,investorTotalWithdrawAmount,totalReturnAmount,publisherAmount,productAmount,closedProductAmount,onSaleProductAmount,activeInvestorAmount,verifiedInvestorAmount "
			+"  FROM T_MONEY_PLATFORM_STATISTICS_HISTORY B "
			+"  WHERE confirmDate BETWEEN ?1 AND ?2 "
			+ " )C ", nativeQuery = true)
	public List<Object[]> platformStatInfo(Date startConfirmDate, Date endConfirmDate);
	
	/**平台首页统计--平台备付金账号信息*/
	@Query(value=" SELECT B.balance, "//#平台备付金余额
			+" B.basicAccBorrowAmount, "//#挪用备付金额
			+" C.toClosedProductAmount "//#待还产品数
			+"FROM T_MONEY_PLATFORM_RESERVEDACCOUNT B "
			+"LEFT JOIN ( "
			+" 	SELECT COUNT(1) toClosedProductAmount "
			+"	FROM T_GAM_PRODUCT A "
			+"	WHERE A.state IN ('RAISING','RAISEEND','DURATIONING','DURATIONEND') "
			+" )C ON 1=1 ",nativeQuery=true)
	public List<Object[]> platformReservedAccountInfo();
	
	/**
	 * 逾期次数
	 */
	@Query("UPDATE PlatformStatisticsEntity  SET overdueTimes = overdueTimes + ?2 "
			+ " WHERE oid = ?1")
	@Modifying
	int increaseOverdueTimes(String oid, int overdueTimes);
	
	/**
	 * 发行人数
	 */
	@Query("UPDATE PlatformStatisticsEntity  SET publisherAmount = publisherAmount + 1 "
			+ " WHERE oid = ?1")
	@Modifying
	int increatePublisherAmount(String oid);
	
	
	/**
	 * 活跃投资人数
	 */
	@Query("UPDATE PlatformStatisticsEntity  SET activeInvestorAmount = ?2 "
			+ " WHERE oid = ?1")
	@Modifying
	int syncActiveInvestorAmount(String oid, int activeInvestorAmount);

	
	
	
	@Query(value=" SELECT platformOid,totalTradeAmount,totalReturnAmount,oid FROM T_MONEY_PLATFORM_STATISTICS where oid > ?1 ", nativeQuery = true)
	List<Object[]> getPlatformStatisticsByBatch(String lastOid);
}
