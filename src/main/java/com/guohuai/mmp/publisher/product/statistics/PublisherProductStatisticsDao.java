package com.guohuai.mmp.publisher.product.statistics;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PublisherProductStatisticsDao extends JpaRepository<PublisherProductStatisticsEntity, String>,
		JpaSpecificationExecutor<PublisherProductStatisticsEntity> {

	
	@Query(value = " DELETE FROM PublisherProductStatisticsEntity WHERE investDate = ?1 ")
	@Modifying
	public int deleteByPubliserOidAndDate(Date date);
	
	/**查询发行人昨日累计投资TOP5产品*/
	@Query(value = "SELECT B.name,A.totalInvestAmount "
					+" FROM T_MONEY_PUBLISHER_PRODUCT_STATISTICS A "
					+" INNER JOIN T_GAM_PRODUCT B ON A.productOid = B.oid "
					+" WHERE A.publisherOid=?1 AND A.investDate =?2 AND A.totalInvestRank <=?3 "
					+" ORDER BY A.totalInvestRank DESC", nativeQuery = true)
	public List<Object[]> findTop5ByPublisherOid(String publisherOid,Date date,Integer rank);
	
	
	/**查询平台昨日新增投资额TOP5产品*/
	@Query(value = "SELECT B.name,A.investAmount "
			+" FROM T_MONEY_PUBLISHER_PRODUCT_STATISTICS A "
			+" INNER JOIN T_GAM_PRODUCT B ON A.productOid = B.oid "
			+" WHERE A.investDate =?1 "
			+" ORDER BY A.investAmount DESC LIMIT 5", nativeQuery = true)
	public List<Object[]> findTop5InvestorOfPlatform(Date date);
}
