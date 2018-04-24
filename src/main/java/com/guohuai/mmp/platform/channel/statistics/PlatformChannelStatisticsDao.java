package com.guohuai.mmp.platform.channel.statistics;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlatformChannelStatisticsDao extends JpaRepository<PlatformChannelStatisticsEntity, String>,
		JpaSpecificationExecutor<PlatformChannelStatisticsEntity> {

	@Query(value = "DELETE FROM PlatformChannelStatisticsEntity WHERE investDate = ?1 ")
	@Modifying
	public int deleteByDate(Date date);

	/**查询渠道昨日累计投资额TOP5*/
	@Query(value = "SELECT B.channelName,C.amount "
			+ "FROM T_MONEY_PLATFORM_CHANNEL B "
			+ "INNER JOIN ("
			+ "   SELECT A.channelOid,SUM(A.totalInvestAmount) amount "
			+ "   FROM T_MONEY_PLATFORM_CHANNEL_STATISTICS A  " 
			+ "   WHERE A.investDate=?1 "
			+ "   GROUP BY A.channelOid DESC " 
			+ "   ORDER BY amount DESC LIMIT 5 "
			+ " )C ON B.oid=C.channelOid", nativeQuery = true)
	@Modifying
	public List<Object[]> queryInvestTop5(Date investDate);

	/**各渠道交易总额*/
	@Query(value = "SELECT B.channelName,C.amount "
			+ "FROM T_MONEY_PLATFORM_CHANNEL B "
			+ "INNER JOIN ("
			+ "   SELECT A.channelOid,SUM(A.totalInvestAmount+A.totalRedeemAmount+A.totalCashAmount) amount "
			+ "   FROM T_MONEY_PLATFORM_CHANNEL_STATISTICS A  " 
			+ "   WHERE A.investDate=?1 "
			+ "   GROUP BY A.channelOid DESC " 
			+ "   ORDER BY amount DESC  "
			+ " )C ON B.oid=C.channelOid", nativeQuery = true)
	@Modifying
	public List<Object[]> queryTotalInvest(Date investDate);
	
	/** 平台交易额占比分析 */
	@Query(value = "SELECT SUM(A.totalInvestAmount),SUM(A.totalRedeemAmount),SUM(A.totalCashAmount) FROM T_MONEY_PLATFORM_CHANNEL_STATISTICS A WHERE A.investDate=?1 ", nativeQuery = true)
	@Modifying
	public List<Object[]> platformTradeAnalyse(Date investDate);
}
