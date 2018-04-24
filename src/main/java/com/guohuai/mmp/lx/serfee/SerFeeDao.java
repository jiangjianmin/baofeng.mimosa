package com.guohuai.mmp.lx.serfee;

import java.math.BigDecimal;
import java.sql.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.product.Product;

public interface SerFeeDao extends JpaRepository<SerFeeEntity, String>, JpaSpecificationExecutor<SerFeeEntity> {

	SerFeeEntity findByProductAndTDay(Product product, Date tDay);
	
	

	@Query(value = "SELECT SUM(fee) FROM SerFeeEntity WHERE channelOid = ?1 ")
	BigDecimal channelSumAccruedFee(String channelOid);

	@Query(value = "SELECT fee FROM T_MONEY_LX_SERFEE WHERE productOid = ?1 and tDay=?2 limit 1", nativeQuery = true)
	BigDecimal findFeeByDate(String productOid, Date tDay);

	@Query("from SerFeeEntity a where a.product.oid = ?1 ")
	public Page<SerFeeEntity> findAccruedFeeByPOid(String productOid, Pageable pageable);
	
}
