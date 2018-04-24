package com.guohuai.mmp.investor.baseaccount;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InvestorBaseAccountDao extends JpaRepository<InvestorBaseAccountEntity, String>, JpaSpecificationExecutor<InvestorBaseAccountEntity> {
	
	@Modifying
	@Query(value = "update T_MONEY_INVESTOR_BASEACCOUNT set updateTime = sysdate() where userOid = ?2 and balance >= ?1", nativeQuery = true)
	public int balanceEnough(BigDecimal moneyVolume, String uid);

	@Query("UPDATE InvestorBaseAccountEntity  SET balance = ?2 WHERE oid = ?1")
	@Modifying
	public int updateBalance(String baseAccountOid, BigDecimal balance);
	
	@Query("UPDATE InvestorBaseAccountEntity  SET balance = balance + ?2 WHERE oid = ?1")
	@Modifying
	public int updateBalancePlusPlus(String baseAccountOid, BigDecimal amount);
	
	@Query("UPDATE InvestorBaseAccountEntity  SET balance = balance - ?2 WHERE oid = ?1 and balance >= ?2 ")
	@Modifying
	public int updateBalanceMinusMinus(String baseAccountOid, BigDecimal amount);
	
	public InvestorBaseAccountEntity findByUserOid(String userOid);
	
	public InvestorBaseAccountEntity findByMemberId(String memberId);
	
	public InvestorBaseAccountEntity findByOwner(String owner);

	public InvestorBaseAccountEntity findByPhoneNum(String phoneNum);
	
	public InvestorBaseAccountEntity findByUid(String uid);
	
	@Query("UPDATE InvestorBaseAccountEntity  SET balance = balance + ?1 WHERE owner = 'platform' and status = 'normal' ")
	@Modifying
	public int borrowFromPlatform(BigDecimal amount);
	
	@Query("UPDATE InvestorBaseAccountEntity  SET balance = balance - ?1 WHERE owner = 'platform' and status = 'normal' and balance >= ?1 ")
	@Modifying
	public int payToPlatform(BigDecimal amount);
	
	@Query(value = "select * from T_MONEY_INVESTOR_BASEACCOUNT where oid > ?1 and owner = 'investor' order by oid limit 2000", nativeQuery = true)
	public List<InvestorBaseAccountEntity> query4Bonus(String lastOid);
	
	@Query("UPDATE InvestorBaseAccountEntity  SET isFreshman = 'no' WHERE oid = ?1 and isFreshman = 'yes' ")
	@Modifying
	public int updateFreshman(String oid);
	
	@Query("UPDATE InvestorBaseAccountEntity  SET isFreshman = 'yes' WHERE oid = ?1 and isFreshman = 'no' ")
	@Modifying
	public int updateFreshman2Yes(String oid);
	
	@Modifying
	@Query(value = "update t_money_investor_baseaccount a,t_money_investor_tradeorder b set a.isFreshman='yes' "
			+ "where a.oid=b.investorOid and b.productOid = ?1 and b.orderStatus = 'confirmed' ", nativeQuery = true)
	public int updateFreshmanBatch(String productOid);
	
	@Query(value = "select memberId from T_MONEY_INVESTOR_BASEACCOUNT where userOid = ?1", nativeQuery = true)
	public String findMemberIdByUserOid(String userOid);

	@Query(value = " SELECT * FROM t_money_investor_baseaccount WHERE oid NOT IN (SELECT investorOid FROM t_money_investor_tradeorder WHERE couponType = 'tasteCoupon') "
			+ " AND OWNER = 'investor' ", nativeQuery = true)
	public List<InvestorBaseAccountEntity> onRegister();
	
	@Query(value = "SELECT bkNum,oid,userOid,orderNum,investorOid,bkNum - orderNum +1 AS bcNum FROM "
			+ "(SELECT COUNT(*) AS bkNum,c.oid AS oid,c.userOid AS userOid FROM "
			+ " (SELECT b.oid AS oid,b.realName AS realName,b.idNum AS idNum,a.refereeOid AS refereeOid FROM "
			+ " T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS a "
			+ " LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT b ON a.investorOid = b.oid "
			+ " WHERE b.realName IS NOT NULL AND b.idNum IS NOT NULL) a "
			+ " LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT_REFEREE b ON a.refereeOid = b.oid "
			+ " LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT c  ON b.investorOid = c.oid "
			+ " GROUP BY c.oid) a "
			+ " INNER JOIN "
			+ " (SELECT COUNT(*) AS orderNum,investorOid FROM T_MONEY_INVESTOR_TRADEORDER "
			+ " WHERE coupons IS NOT NULL AND orderStatus IN ('submitted', 'confirmed')  GROUP BY investorOid) b "
			+ " ON a.oid = b.investorOid AND (b.orderNum - a.bkNum) <= 0 ", nativeQuery = true)
	public List<Object[]> getNeedCompensateRefereeCoupon();
	
	@Query(value = " SELECT * FROM t_money_investor_baseaccount WHERE status='writeOff'", nativeQuery = true)
	public List<InvestorBaseAccountEntity> findByWriteOffStatus();

	@Query("UPDATE InvestorBaseAccountEntity  SET status = 'normal' WHERE userOid = ?1 ")
	@Modifying
	public int updateBaseAccountStatus(String userOid);
	
	@Query(value="UPDATE t_money_investor_baseaccount SET phoneNum = ?3 WHERE oid = ?1 AND phoneNum = ?2", nativeQuery = true)
	@Modifying
	public int updateBaseAccountPhoneNum(String userOid, String oldPhone, String newPhone);
}
