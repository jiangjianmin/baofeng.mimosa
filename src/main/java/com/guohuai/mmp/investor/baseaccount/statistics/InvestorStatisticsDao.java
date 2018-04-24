package com.guohuai.mmp.investor.baseaccount.statistics;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;

public interface InvestorStatisticsDao extends JpaRepository<InvestorStatisticsEntity, String>, JpaSpecificationExecutor<InvestorStatisticsEntity> {
	
	
	/**
	 * 充值
	 * @param baseAccount
	 * @param orderAmount
	 * @return int
	 */
	@Query("UPDATE InvestorStatisticsEntity a SET a.totalDepositAmount = a.totalDepositAmount + ?2,"
			+ " a.totalDepositCount = a.totalDepositCount + 1,"
			+ " a.todayDepositCount = a.todayDepositCount + 1,"
			+ " a.todayDepositAmount = a.todayDepositAmount + ?2 "
			+ " WHERE a.investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4Deposit(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount);
	
	/**
	 * 投资人提现回调
	 * @param baseAccount
	 * @param orderAmount
	 * @return int
	 */
	@Query("UPDATE InvestorStatisticsEntity a SET a.totalWithdrawAmount = a.totalWithdrawAmount + ?2,"
			+ " a.totalWithdrawCount = a.totalWithdrawCount + 1,"
			+ " a.todayWithdrawCount = a.todayWithdrawCount + 1,"
			+ " a.todayWithdrawAmount = a.todayWithdrawAmount + ?2"
			+ "  WHERE a.investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4Withdraw(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount);
	
	
	/**
	 * 活期--赎回
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query("UPDATE InvestorStatisticsEntity a SET a.totalRedeemAmount = a.totalRedeemAmount + ?2,"
			+ " a.totalRedeemCount = a.totalRedeemCount + 1,"
			+ " a.todayRedeemCount = a.todayRedeemCount + 1,"
			+ " a.todayRedeemAmount = a.todayRedeemAmount + ?2,"
			+ " a.t0CapitalAmount = a.t0CapitalAmount - ?2" 
			+ " WHERE a.investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4T0Redeem(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount);
	
	/**
	 * 定期--还本
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query("UPDATE InvestorStatisticsEntity  SET totalRepayLoan = totalRepayLoan + ?2, tnCapitalAmount = tnCapitalAmount - ?2 WHERE investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4TnRepayLoan(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount);
	
	/**
	 * 定期--付息
	 * @param oid
	 * @param orderAmount
	 * @return
	 */
	@Query("UPDATE InvestorStatisticsEntity SET tnCapitalAmount = tnCapitalAmount - ?2 WHERE investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4TnRepayInterest(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount);
	
	/**
	 * 活期--投资
	 * @param baseAccount
	 * @param orderAmount
	 * @return int
	 */
	@Query("UPDATE InvestorStatisticsEntity a SET a.totalInvestAmount = a.totalInvestAmount + ?2, "
			+ "a.totalInvestCount = a.totalInvestCount + 1, "
			+ "a.todayInvestCount = a.todayInvestCount + 1, "
			+ "a.todayInvestAmount = a.todayInvestAmount + ?2, "
			+ "a.totalInvestProducts = ?3, "
			+ "a.t0CapitalAmount = a.t0CapitalAmount + ?2, a.updateTime = sysdate() WHERE a.investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4T0Invest(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount, int totalInvestProducts);
	
	/**
	 * 活期--第一笔投资
	 * @param baseAccount
	 * @param orderAmount
	 * @return int
	 */
	@Query("UPDATE InvestorStatisticsEntity a SET a.totalInvestAmount = a.totalInvestAmount + ?2, "
			+ "a.totalInvestCount = a.totalInvestCount + 1, "
			+ "a.todayInvestCount = a.todayInvestCount + 1, "
			+ "a.todayInvestAmount = a.todayInvestAmount + ?2, "
			+ "a.t0CapitalAmount = a.t0CapitalAmount + ?2, "
			+ "a.totalInvestProducts = ?3, "
			+ "a.firstInvestTime = sysdate(), a.updateTime = sysdate() WHERE a.investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4T0InvestFtime(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount, int totalInvestProducts);
	
	/**
	 * 定期投资
	 * @param baseAccount
	 * @param orderAmount
	 * @return int
	 */
	@Query("UPDATE InvestorStatisticsEntity a SET a.totalInvestAmount = a.totalInvestAmount + ?2, "
			+ "a.totalInvestCount = a.totalInvestCount + 1, "
			+ "a.todayInvestCount = a.todayInvestCount + 1, "
			+ "a.todayInvestAmount = a.todayInvestAmount + ?2, "
			+ "a.totalInvestProducts = ?3, "
			+ "a.tnCapitalAmount = a.tnCapitalAmount + ?2, a.updateTime = sysdate() WHERE a.investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4TnInvest(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount, int totalInvestProducts);
	
	/**
	 * 定期第一笔投资
	 * @param baseAccount
	 * @param orderAmount
	 * @return int
	 */
	@Query("UPDATE InvestorStatisticsEntity a SET a.totalInvestAmount = a.totalInvestAmount + ?2, "
			+ "a.totalInvestCount = a.totalInvestCount + 1, "
			+ "a.todayInvestCount = a.todayInvestCount + 1, "
			+ "a.todayInvestAmount = a.todayInvestAmount + ?2, "
			+ "a.tnCapitalAmount = a.tnCapitalAmount + ?2, "
			+ "a.totalInvestProducts = ?3, "
			+ "a.firstInvestTime = sysdate(), a.updateTime = sysdate() WHERE a.investorBaseAccount = ?1")
	@Modifying
	public int updateStatistics4TnInvestFtime(InvestorBaseAccountEntity baseAccount, BigDecimal orderAmount, int totalInvestProducts);

	public InvestorStatisticsEntity findByInvestorBaseAccount(InvestorBaseAccountEntity baseAccount);

	
	@Query(value = "update InvestorStatisticsEntity set updateTime = sysdate() where todayWithdrawAmount > ?2 and investorBaseAccount = ?1")
	@Modifying
	public int checkTodayWithdrawCount(InvestorBaseAccountEntity baseAccount, int wdFreeTimes);
	
	
	/**
	 * totalIncomeAmount 累计收益总额
	 * t0YesterdayIncome 活期昨日收益额
	 * t0TotalIncome 活期总收益额
	 * t0CapitalAmount 活期资产总额
	 */
	@Query(value = "UPDATE InvestorStatisticsEntity set totalIncomeAmount = totalIncomeAmount + ?2 + ?3, t0YesterdayIncome = ?2 +?3,"
			+ " t0TotalIncome = t0TotalIncome + ?2 + ?3, t0CapitalAmount = t0CapitalAmount + ?2 + ?3, incomeConfirmDate = ?4 where investorBaseAccount = ?1")
	@Modifying
	public int interestStatistics(InvestorBaseAccountEntity investorBaseAccount, BigDecimal holdIncomeAmount, BigDecimal holdLockIncomeAmount, Date incomeDate);
	
	/**
	 * totalIncomeAmount 累计收益总额
	 * tnTotalIncome 定期总收益额
	 * @return
	 */
	@Query(value = "UPDATE InvestorStatisticsEntity set tnCapitalAmount = tnCapitalAmount + ?2 + ?3, totalIncomeAmount = totalIncomeAmount + ?2 + ?3, tnTotalIncome = tnTotalIncome + ?2 + ?3,"
			+ " incomeConfirmDate = ?4 where investorBaseAccount = ?1")
	@Modifying
	public int interestStatisticsTn(InvestorBaseAccountEntity investorBaseAccount, BigDecimal holdIncomeAmount, BigDecimal holdLockIncomeAmount, Date incomeDate);
	
	
	@Query(value = "UPDATE InvestorStatisticsEntity set todayDepositCount = 0, todayWithdrawCount = 0, "
			+ "todayInvestCount = 0, todayRedeemCount = 0,"
			+ "todayDepositAmount = 0, todayWithdrawAmount = 0, todayInvestAmount = 0, todayRedeemAmount = 0")
	@Modifying
	public int resetToday();
	
	@Query(value = "UPDATE InvestorStatisticsEntity set todayDepositCount = 0, todayWithdrawCount = 0, "
			+ "todayInvestCount = 0, todayRedeemCount = 0,"
			+ "todayDepositAmount = 0, todayWithdrawAmount = 0, todayInvestAmount = 0, todayRedeemAmount = 0 where oid = ?1 ")
	@Modifying
	public int resetToday(String investorStatisticsOid);
	
	@Query(value = "select * from T_MONEY_INVESTOR_STATISTICS where oid > ?1 and (todayDepositCount > 0 or todayWithdrawCount > 0 or todayInvestCount > 0 or todayRedeemCount > 0)  order by oid limit 2000 ", nativeQuery = true)
	public List<InvestorStatisticsEntity> getResetTodayInvestorStatistics(String lastOid);
}
