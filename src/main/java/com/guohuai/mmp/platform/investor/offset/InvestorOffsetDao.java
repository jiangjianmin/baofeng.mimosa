package com.guohuai.mmp.platform.investor.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InvestorOffsetDao extends JpaRepository<InvestorOffsetEntity, String>, JpaSpecificationExecutor<InvestorOffsetEntity> {
	@Query(value = "select * from T_MONEY_PLATFORM_INVESTOR_OFFSET "
			+ "where substr(offsetCode, 9) != date_format(sysdate(), '%H') and clearStatus = 'toClear' and offsetFrequency = 'fast'", nativeQuery = true)
	public List<InvestorOffsetEntity> getToClearOffset();
	
	@Query("from InvestorOffsetEntity where offsetCode = ?1 and offsetFrequency = 'fast'")
	public InvestorOffsetEntity getLatestFastOffset(String offsetCode);
	
	@Query("from InvestorOffsetEntity where offsetCode = ?1 and offsetFrequency = 'normal' ")
	public InvestorOffsetEntity getLatestNormalOffset(String offsetCode);
	
	@Query("update InvestorOffsetEntity set redeemAmount = redeemAmount + ?2, toCloseRedeemAmount = toCloseRedeemAmount + ?3 where oid = ?1")
	@Modifying
	public int increaseRedeem(String oid, BigDecimal redeemAmount, int step);
	
	@Query("update InvestorOffsetEntity set clearStatus = ?2 where oid = ?1 and clearStatus = 'toClear'")
	@Modifying
	public int updateClearStatus(String oid, String clearStatus);
	
	@Query(value="update T_MONEY_PLATFORM_INVESTOR_OFFSET set toCloseRedeemAmount = toCloseRedeemAmount - 1,"
			+ "closeStatus = IF(toCloseRedeemAmount = 0, 'closed', closeStatus) where oid = ?1", nativeQuery = true)
	@Modifying
	public int reduceToCloseRedeemAmount(String oid);
	
	@Query(value="update T_MONEY_PLATFORM_INVESTOR_OFFSET set toCloseRedeemAmount = toCloseRedeemAmount - ?2,"
			+ "closeStatus = IF(toCloseRedeemAmount = 0, 'closed', closeStatus) where oid = ?1", nativeQuery = true)
	@Modifying
	public int reduceToCloseRedeemAmount(String oid, int num);
	
	@Query("update InvestorOffsetEntity set closeStatus = ?2 where oid = ?1 and clearStatus = 'cleared' and closeStatus = 'closing' ")
	@Modifying
	public int updateCloseStatus(String oid, String closeStatus);
	
	@Query("update InvestorOffsetEntity set closeStatus = ?2 where oid = ?1 and clearStatus = 'cleared' and closeStatus = 'toClose' ")
	@Modifying
	public int updateCloseStatusDirectly(String oid, String closeStatus);
	
	@Query("update InvestorOffsetEntity set closeStatus = ?2, closeMan = ?3 where oid = ?1 "
			+ " and clearStatus = 'cleared' and closeStatus in ('toClose','closeSubmitFailed','closePayFailed')")
	@Modifying
	public int updateCloseStatus4Lock(String iOffsetOid, String closeStatus, String closeMan);
	
	@Query(value = "from InvestorOffsetEntity where offsetDate > ?1 and overdueStatus != 'yes'"
			+ " and closeStatus != 'closed' ")
	public List<InvestorOffsetEntity> getOverdueInvestorOffset(Date curDate);
	
	@Query(value = "select count(*) from T_MONEY_PLATFORM_INVESTOR_OFFSET where offsetDate < ?1 and offsetFrequency = 'normal' and (clearStatus = 'toClear' or closeStatus = 'toClose') ", nativeQuery = true)
	public int beforeOffsetDate(Date offsetDate);
}
