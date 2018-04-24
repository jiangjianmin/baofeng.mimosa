package com.guohuai.mmp.platform.reserved.account;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ReservedAccountDao extends JpaRepository<ReservedAccountEntity, String>, JpaSpecificationExecutor<ReservedAccountEntity> {
	
	
	/**
	 * 平台户代收
	 */
	@Query(value =  "update ReservedAccountEntity set basicAccBorrowAmount = basicAccBorrowAmount + ?1, "
			+ "lastBorrowTime = sysdate() where oid = ?2")
	@Modifying
	int update4BasicCollect(BigDecimal orderAmount, String oid);
	
	/**
	 * 平台户代付
	 */
	@Query(value =  "update ReservedAccountEntity set basicAccBorrowAmount = basicAccBorrowAmount - ?1, "
			+ "lastBorrowTime = sysdate() where oid = ?2 and basicAccBorrowAmount >= ?1")
	@Modifying
	int update4BasicPay(BigDecimal orderAmount, String oid);
	
	/**
	 * 超级户代收
	 */
	@Query(value =  "update ReservedAccountEntity set superAccBorrowAmount = superAccBorrowAmount + ?1,"
			+ " lastBorrowTime = sysdate() where oid = ?2")
	@Modifying
	int update4SuperCollect(BigDecimal orderAmount, String oid);
	
	/**
	 * 超级户代付
	 */
	@Query(value =  "update ReservedAccountEntity set superAccBorrowAmount = superAccBorrowAmount - ?1,"
			+ " lastBorrowTime = sysdate() where oid = ?2 and superAccBorrowAmount >= ?1")
	@Modifying
	int update4SuperPay(BigDecimal orderAmount, String oid);
	
	/**
	 * 备付金余额调整 
	 */
	@Query(value =  "update ReservedAccountEntity set  balance = balance + ?1 where oid = ?2")
	@Modifying
	int updateBalancePlusPlus(BigDecimal orderAmount, String oid);
	
	/**
	 * 备付金余额调减 
	 */
	@Query(value =  "update ReservedAccountEntity set  balance = balance - ?1 where oid = ?2")
	@Modifying
	int updateBalanceMinusMinus(BigDecimal orderAmount, String oid);
	
	/**
	 * 运营户余额调整 
	 */
	@Query(value =  "update ReservedAccountEntity set  operationAccBorrowAmount = operationAccBorrowAmount + ?1 where oid = ?2")
	@Modifying
	int updateOperationBalancePlusPlus(BigDecimal orderAmount, String oid);
	
	/**
	 * 运营户余额调减
	 */
	@Query(value =  "update ReservedAccountEntity set  operationAccBorrowAmount = operationAccBorrowAmount - ?1 where oid = ?2")
	@Modifying
	int updateOperationBalanceMinusMinus(BigDecimal orderAmount, String oid);
	
	/**
	 * 查询备付金账户
	 * @return
	 */
	@Query(value = "from ReservedAccountEntity ")
	ReservedAccountEntity getReservedAccount();
	
	/**
	 * 累计充值总额
	 */
	@Query(value =  "update ReservedAccountEntity set  totalDepositAmount = totalDepositAmount + ?1 where oid = ?2")
	@Modifying
	int increaseTotalDepositAmount(BigDecimal orderAmount, String oid);
	
	/**
	 * 累计提现总额
	 */
	@Query(value =  "update ReservedAccountEntity set  totalWithdrawAmount = totalWithdrawAmount + ?1 where oid = ?2")
	@Modifying
	int increaseTotalWithdrawAmount(BigDecimal orderAmount, String oid);
	

	

	

}
