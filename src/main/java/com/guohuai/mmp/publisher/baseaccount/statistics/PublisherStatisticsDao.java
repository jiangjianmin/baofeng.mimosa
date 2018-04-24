package com.guohuai.mmp.publisher.baseaccount.statistics;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

public interface PublisherStatisticsDao extends JpaRepository<PublisherStatisticsEntity, String>, JpaSpecificationExecutor<PublisherStatisticsEntity> {

	
	/**
	 * 充值
	 * @param baseAccount
	 * @param orderAmount
	 * @return int
	 */
	@Query("UPDATE PublisherStatisticsEntity a SET a.totalDepositAmount = a.totalDepositAmount + ?2, a.updateTime = sysdate() WHERE a.publisherBaseAccount = ?1")
	@Modifying
	public int updateStatistics4Deposit(PublisherBaseAccountEntity baseAccount, BigDecimal orderAmount);
	
	public PublisherStatisticsEntity findByPublisherBaseAccount(PublisherBaseAccountEntity baseAccount);
	
	/**
	 * 提现
	 * @param publisherBaseAccount
	 * @param orderAmount
	 * @return
	 */
	@Query("UPDATE PublisherStatisticsEntity a SET a.totalWithdrawAmount = a.totalWithdrawAmount + ?2, a.updateTime = sysdate() WHERE a.publisherBaseAccount = ?1")
	@Modifying
	public int updateStatistics4Withdraw(PublisherBaseAccountEntity publisherBaseAccount, BigDecimal orderAmount);
	
	/**
	 * 借款
	 * @param publisherBaseAccount
	 * @param orderAmount
	 * @return
	 */
	@Query("UPDATE PublisherStatisticsEntity a SET a.totalLoanAmount = a.totalLoanAmount + ?2  WHERE a.publisherBaseAccount = ?1")
	@Modifying
	public int increaseTotalLoanAmount(PublisherBaseAccountEntity publisherBaseAccount, BigDecimal orderAmount);
	
	
	/**
	 * 还款
	 * @param publisherBaseAccount
	 * @param orderAmount
	 * @return
	 */
	@Query("UPDATE PublisherStatisticsEntity a SET a.totalReturnAmount = a.totalReturnAmount + ?2  WHERE a.publisherBaseAccount = ?1")
	@Modifying
	public int increaseTotalReturnAmount(PublisherBaseAccountEntity publisherBaseAccount, BigDecimal orderAmount);
	
	@Query("UPDATE PublisherStatisticsEntity  SET todayInvestAmount = 0, todayT0InvestAmount = 0, "
			+ " todayTnInvestAmount = 0, todayRedeemAmount = 0,"
			+ " todayRepayInvestAmount = 0, todayRepayInterestAmount = 0,"
			+ " todayT0InvestorAmount = 0, todayTnInvestorAmount = 0,"
			+ " todayInvestorAmount = 0")
	@Modifying
	public int resetToday();
	
	/**
	 * 发行产品数
	 */
	@Query("UPDATE PublisherStatisticsEntity  SET productAmount = productAmount + 1 WHERE publisherBaseAccount = ?1")
	@Modifying
	public int increaseReleasedProductAmount(PublisherBaseAccountEntity baseAccount);
	
	/**
	 * 在售
	 */
	@Query("UPDATE PublisherStatisticsEntity  SET onSaleProductAmount = onSaleProductAmount + 1 WHERE publisherBaseAccount = ?1")
	@Modifying
	public int increaseOnSaleProductAmount(PublisherBaseAccountEntity baseAccount);
	
	/**
	 * 待结算
	 */
	@Query("UPDATE PublisherStatisticsEntity  SET onSaleProductAmount = onSaleProductAmount - 1, "
			+ "toCloseProductAmount = toCloseProductAmount + 1 WHERE publisherBaseAccount = ?1")
	@Modifying
	public int increaseToCloseProductAmount(PublisherBaseAccountEntity baseAccount);
	
	/**
	 * 已结算
	 */
	@Query("UPDATE PublisherStatisticsEntity  SET toCloseProductAmount = toCloseProductAmount - 1, "
			+ "closedProductAmount = closedProductAmount + 1 WHERE publisherBaseAccount = ?1")
	@Modifying
	public int increaseClosedProductAmount(PublisherBaseAccountEntity baseAccount);

	/**发行人统计信息*/
	@Query(value = "SELECT "
					+" B.accountBalance, "//余额
					+" A.totalLoanAmount, "//累计借款总额
					+" A.totalReturnAmount, "//累计还款总额
					+" A.investorAmount, "//总投资人数
					+" A.investorHoldAmount, "//现持仓人数
					+" A.totalDepositAmount, "//累计充值总额
					+" A.totalWithdrawAmount  "//累计提现总额
					+" FROM T_MONEY_PUBLISHER_BASEACCOUNT B  "
					+" LEFT JOIN T_MONEY_PUBLISHER_STATISTICS A  ON A.publisherOid = B.oid "
					+" WHERE B.oid = ?1 ", nativeQuery = true)
	public List<Object[]> queryByPublisherOid(String publisherOid);
	

	/**
	 *投资人数、持仓人数 
	 */
	@Query("UPDATE PublisherStatisticsEntity  SET investorAmount = investorAmount + 1, investorHoldAmount = investorHoldAmount + 1"
			+ " WHERE publisherBaseAccount = ?1")
	@Modifying
	public int increaseInvestorAmount(PublisherBaseAccountEntity publisherBaseAccount);
	
	/**
	 * 逾期次数
	 */
	@Query("UPDATE PublisherStatisticsEntity  SET overdueTimes = overdueTimes + 1"
			+ " WHERE publisherBaseAccount = ?1")
	@Modifying
	public int increaseOverdueTimes(PublisherBaseAccountEntity publisherBaseAccount);
	
	/**
	 * 累计付息总额
	 */
	@Query("UPDATE PublisherStatisticsEntity  SET totalInterestAmount = totalInterestAmount + ?2"
			+ " WHERE publisherBaseAccount = ?1")
	@Modifying
	public int increaseTotalInterestAmount(PublisherBaseAccountEntity publisherBaseAccount, BigDecimal successAllocateIncome);

	@Query(value = "select publisherOid,toCloseProductAmount,closedProductAmount,oid from T_MONEY_PUBLISHER_STATISTICS where oid > ?1 ", nativeQuery = true)
	public List<Object[]> getPublisherStatisticsByBatch(String lastOid);
	
	

}
