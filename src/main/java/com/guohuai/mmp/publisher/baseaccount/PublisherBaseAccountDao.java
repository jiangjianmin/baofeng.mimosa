package com.guohuai.mmp.publisher.baseaccount;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PublisherBaseAccountDao extends JpaRepository<PublisherBaseAccountEntity, String>, JpaSpecificationExecutor<PublisherBaseAccountEntity> {

	@Query(value = "update PublisherBaseAccountEntity set accountBalance = ?2 where oid = ?1")
	@Modifying
	public int updateBalance(String oid, BigDecimal orderAmount);
	
	
	@Query(value = "update PublisherBaseAccountEntity set accountBalance = accountBalance + ?2 where oid = ?1")
	@Modifying
	public int updateBalancePlusPlus(String oid, BigDecimal orderAmount);
	
	@Query(value = "update PublisherBaseAccountEntity set accountBalance = accountBalance - ?2 where oid = ?1")
	@Modifying
	public int updateBalanceMinusMinus(String oid, BigDecimal orderAmount);
	
	@Query(value = "update PublisherBaseAccountEntity set updateTime = sysdate() where oid = ?2 and accountBalance >= ?1")
	@Modifying
	public int balanceEnough(BigDecimal orderAmount, String uid);


	public PublisherBaseAccountEntity findByCorperateOid(String corperate);

	@Query("from PublisherBaseAccountEntity s where s.status = ?1 order by s.updateTime desc")
	public List<PublisherBaseAccountEntity> findByStatus(String status);
	

	@Query(value="SELECT oid,corperateOid FROM T_MONEY_PUBLISHER_BASEACCOUNT",nativeQuery=true)
	public List<Object[]> findOneOid();
}
