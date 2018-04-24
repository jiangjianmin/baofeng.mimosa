package com.guohuai.mmp.platform.publisher.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

public interface PublisherOffsetDao extends JpaRepository<PublisherOffsetEntity, String>, JpaSpecificationExecutor<PublisherOffsetEntity> {
	@Query("from PublisherOffsetEntity where offsetCode = ?2 and publisherBaseAccount = ?1")
	public PublisherOffsetEntity getLatestOffset(PublisherBaseAccountEntity publisherBaseAccount,String offsetCode);

	@Query("update PublisherOffsetEntity set investAmount = investAmount +?2, "
			+ " netPosition = investAmount - redeemAmount where oid=?1")
	@Modifying
	public int increaseInvest(String oid, BigDecimal investAmount);
	
	@Query("update PublisherOffsetEntity set redeemAmount = redeemAmount +?2,netPosition =investAmount-redeemAmount where oid=?1")
	@Modifying
	public int increaseRedeem(String oid,BigDecimal redeemAmount);
	
	@Query("update PublisherOffsetEntity set confirmStatus = ?2 where oid = ?1 and confirmStatus = 'confirming' ")
	@Modifying
	public int updateConfirmStatus(String offsetOid, String confirmStatus);
	
	@Query(value = "UPDATE T_MONEY_PLATFORM_PUBLISHER_OFFSET SET toCloseRedeemAmount = toCloseRedeemAmount - 1, closeStatus = IF(toCloseRedeemAmount = 0, 'closed', 'closing'), updateTime = SYSDATE() where oid = ?1", nativeQuery = true)
	@Modifying
	public int decreaseToCloseRedeemAmount(String oid);
	
	@Query(value = "UPDATE T_MONEY_PLATFORM_PUBLISHER_OFFSET SET updateTime = SYSDATE() where oid = ?1 and closeStatus = 'closed' ", nativeQuery = true)
	@Modifying
	public int isClosed(String oid);

	@Query("update PublisherOffsetEntity set confirmStatus = ?2 where oid = ?1 and clearStatus = 'cleared' and confirmStatus in ('toConfirm','confirmFailed')")
	@Modifying
	public int updateConfirmStatus4Lock(String offsetOid, String confirmStatus);
	
	/**
	 * 清算时更新轧差状态
	 * @param oid
	 * @param offsetOffsetstatusCleared
	 */
	@Query("update PublisherOffsetEntity set clearStatus = ?2, closeStatus = ?3 where oid = ?1 and clearStatus = 'toClear' and closeStatus = 'toClose' ")
	@Modifying
	public int updateOffsetStatusCloseStatus4Clear(String offsetOid, String clearStatus, String closeStatus);
	
	@Query("update PublisherOffsetEntity set clearStatus = ?2 where oid = ?1 and clearStatus = 'toClear' ")
	@Modifying
	public int updateClearStatus(String oid, String clearStatus);
	/**
	 * 结算时更新结算状态
	 * @param oid
	 * @param closeStatus
	 */
	@Query("update PublisherOffsetEntity set closeStatus = ?2, closeMan = ?3"
			+ " where oid = ?1 and clearStatus = 'cleared' and closeStatus != 'closed' ")
	@Modifying
	public int updateCloseStatus4Close(String offsetOid, String closeStatus, String closeMan);
	
	@Query("update PublisherOffsetEntity set closeStatus = ?2 "
			+ "where oid = ?1 and closeStatus != 'closed' ")
	@Modifying
	public int updateCloseStatus4CloseBack(String offsetOid, String closeStatus);
	
	public PublisherOffsetEntity findByPublisherBaseAccountAndOffsetCode(PublisherBaseAccountEntity spv, String offsetCode);
	
	@Query(value = "from PublisherOffsetEntity where offsetDate > ?1 and overdueStatus != 'yes'"
			+ " and (confirmStatus != 'confirmed' or closeStatus != 'closed') ")
	public List<PublisherOffsetEntity> getOverdueOffset(Date curDate);
	
	@Query(value = "select count(*) from T_MONEY_PLATFORM_PUBLISHER_OFFSET "
			+ " where offsetDate < ?1 and publisherOid = ?2  and (clearStatus = 'toClear' or confirmStatus = 'toConfirm' or closeStatus = 'toClose') ", nativeQuery = true)
	public int beforeOffsetDate(Date offsetDate, String publisherOid);

}
