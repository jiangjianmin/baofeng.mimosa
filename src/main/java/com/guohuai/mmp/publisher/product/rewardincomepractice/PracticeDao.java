package com.guohuai.mmp.publisher.product.rewardincomepractice;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.product.Product;

public interface PracticeDao extends JpaRepository<PracticeEntity, String>, JpaSpecificationExecutor<PracticeEntity> {

	
	@Query(value = "select count(*) from PracticeEntity where product = ?1 and tDate = ?2")
	int countByProductAndTDate(Product product, Date sqlDate);

	@Query("select max(tDate) from PracticeEntity  where product = ?1")
	Date findMaxTDate(Product product);
	
	
	@Query("from PracticeEntity  where product = ?1 and tDate = ?2 and reward is not null order by reward.startDate asc")
	List<PracticeEntity> findByProductAndTDate(Product product, Date tDate);
	
	@Query("from PracticeEntity  where product = ?1 and tDate = ?2 and reward is null")
	PracticeEntity findRewardIsNull(Product product, Date tDate);
	
	@Query(value = "select *  from  T_MONEY_PUBLISHER_PRODUCT_REWARDINCOMEPRACTICE  where productOid = ?1 and rewardRuleOid is null order by tDate desc limit 1", nativeQuery = true)
	PracticeEntity findRewardIsNull(String productOid);

	
	
	@Query("from PracticeEntity  where product = ?1 and tDate = ?2")
	List<PracticeEntity> findByPrductAfterInterest(Product product, Date incomeDate);
	
	@Modifying
	@Query(value = "delete from T_MONEY_PUBLISHER_PRODUCT_REWARDINCOMEPRACTICE where productOid = ?1 and tDate = ?2",nativeQuery=true)
	int delByProductAndTDate(String productOid, Date sqlDate);
	
	
}
