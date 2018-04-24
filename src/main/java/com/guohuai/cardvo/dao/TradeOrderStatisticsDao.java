package com.guohuai.cardvo.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.guohuai.cardvo.entity.TradeOrderStatisticsEntity;

/**
 * @Desc: 订单统计
 * @author huyong
 * @date 2017.10.24
 */
public interface TradeOrderStatisticsDao extends JpaRepository<TradeOrderStatisticsEntity, String>,JpaSpecificationExecutor<TradeOrderStatisticsEntity> {

	/**
	 * @Desc: 根据投资人id查询是否存在数据
	 * @author huyong
	 * @date 2017.10.24
	 */
	@Query(value="select count(1) from t_money_investor_tradeorder_statistics t1 where t1.investorOid = ?1", nativeQuery=true)
	public int findByInvestorOid(String investorOid);
	
	@Query(value="select t1 from TradeOrderStatisticsEntity t1 where t1.investorOid = ?1")
	public TradeOrderStatisticsEntity findEntityByOid(String investorOid);
	
	/**
	 * @Desc: 根据投资人id根据累计投资金额、笔数
	 * @author huyong
	 * @date 2017.10.24
	 */
	@Modifying
	@Query(value="update t_money_investor_tradeorder_statistics t1 set t1.totalInvestAmount = t1.totalInvestAmount + ?2,t1.totalInvestCount = t1.totalInvestCount + 1,t1.lastTradeTime = ?3 where t1.investorOid = ?1 ",nativeQuery = true)
	public int updateByInvestorOid(String investorOid, BigDecimal totalInvestAmount,Timestamp orderTime);
	
	/**
	 * @Desc: 根据投资人list id查询
	 * @author huyong
	 * @date 2017.10.24
	 */
	@Query(value="select * from t_money_investor_tradeorder_statistics t1 where t1.investorOid in (?1)", nativeQuery=true)
	public List<TradeOrderStatisticsEntity> findListByInvestorOid(List<String> investorOidList);
}
