package com.guohuai.mmp.platform.statistics;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserBehaviorStatisticsDao extends JpaRepository<UserInvestStatisticsEntity, String>,JpaSpecificationExecutor<UserInvestStatisticsEntity> {
	/**
	 * 
	 * @author yihonglei
	 * @Title: getUserInvestBehaviorStatSum
	 * @Description: 用户行为总计
	 * @param channelOid
	 * @return List<Object[]>
	 * @date 2017年6月27日 下午11:33:05
	 * @since  1.0.0
	 */
	@Query(value=
			  " SELECT "
			+ "   SUM(uis.registerNum) registerNumSum, "
			+ "   SUM(uis.bindCardNum) bindCardNumSum, "
			+ "   SUM(uis.investorPeopleNum+uis.investorPeopleGoldNum) investorPeopleNumSum, "
			+ "   SUM(uis.orderAmount+uis.orderAmountGold) orderAmountSum, "
			+ "   SUM(uis.investorPenNum+uis.investorPenGoldNum) investorPenNum, "
			+ "   SUM((uis.orderAmount+uis.orderAmountGold)/(uis.investorPeopleNum+uis.investorPeopleGoldNum)) investorAvgAmountSum "
			+ " FROM t_user_invest_statistics uis "
			+ " WHERE IF(?1 IS NULL OR ?1 = '', 1=1, uis.channelOid = ?1) ",nativeQuery=true)
	public List<Object[]> getUserInvestBehaviorStatSum(String channelOid);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getUserInvestBehaviorByDailyStatisticsList
	 * @Description:按日统计用户投资行为
	 * @param startTime
	 * @param endTime
	 * @param isExperGold
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年5月11日 下午11:54:55
	 * @since  1.0.0
	 */
	@Query(value=
			  " SELECT "
			+ "   uis.sortTime sortTime, "
			+ "   SUM(uis.registerNum) registerNum, "
			+ "   SUM(uis.bindCardNum) bindCardNum, "
			+ "   CASE WHEN 1 = ?3 THEN SUM(uis.investorPeopleNum+uis.investorPeopleGoldNum) ELSE SUM(uis.investorPeopleNum) END investorPeopleNum, "
			+ "   CASE WHEN 1 = ?3 THEN SUM(uis.orderAmount+uis.orderAmountGold) ELSE SUM(uis.orderAmount) END orderAmount, "
			+ "   CASE WHEN 1 = ?3 THEN SUM(uis.investorPenNum+uis.investorPenGoldNum) ELSE SUM(uis.investorPenNum) END investorPenNum, "
			+ "   CASE WHEN 1 = ?3 THEN SUM((uis.orderAmount+uis.orderAmountGold)/(uis.investorPeopleNum+uis.investorPeopleGoldNum)) ELSE SUM(uis.orderAmount/uis.investorPeopleNum) END investorAvgAmount "
			+ " FROM t_user_invest_statistics uis "
			+ " WHERE uis.sortTime BETWEEN ?1 AND ?2 "
			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1, uis.channelOid = ?4) "
			+ " GROUP BY uis.sortTime ORDER BY uis.sortTime DESC LIMIT ?5,?6 ",nativeQuery=true)
	public List<Object[]> getUserInvestBehaviorByDailyStatisticsList(
			String startTime, String endTime, int isExperGold, String channelOid,
			int pageRow, int row);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getPageCountDaily
	 * @Description:按日统计总条数查询
	 * @return int
	 * @date 2017年5月8日 上午11:25:34
	 * @since  1.0.0
	 */
	@Query(value=" SELECT COUNT(1) FROM ( SELECT COUNT(1) FROM t_user_invest_statistics uis WHERE uis.sortTime BETWEEN ?1 AND ?2 AND IF(?3 IS NULL OR ?3 = '', 1=1, uis.channelOid = ?3) GROUP BY uis.sortTime ) countStat " ,nativeQuery=true)
	public int getPageCountDaily(String startTime, String endTime, String channelOid);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getUserInvestBehaviorByMonthStatisticsList
	 * @Description:按月统计用户投资行为
	 * @param channelOid
	 * @param startTime
	 * @param endTime
	 * @param isExperGold
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年5月8日 上午11:06:23
	 * @since  1.0.0
	 */
	@Query(value=
			  " SELECT "
			+ "   DATE_FORMAT(uis.sortTime,'%Y-%m') sortTome, "
			+ "   SUM(uis.registerNum) registerNum, "
			+ "   SUM(uis.bindCardNum) bindCardNum, "
			+ "   CASE WHEN 1 = ?3 THEN SUM(uis.investorPeopleNum+uis.investorPeopleGoldNum) ELSE SUM(uis.investorPeopleNum) END investorPeopleNum, "
			+ "   CASE WHEN 1 = ?3 THEN SUM(uis.orderAmount+uis.orderAmountGold) ELSE SUM(uis.orderAmount) END orderAmount, "
			+ "   CASE WHEN 1 = ?3 THEN SUM(uis.investorPenNum+uis.investorPenGoldNum) ELSE SUM(uis.investorPenNum) END investorPenNum, "
			+ "   CASE WHEN 1 = ?3 THEN SUM(IFNULL((uis.orderAmount+uis.orderAmountGold)/(uis.investorPeopleNum+uis.investorPeopleGoldNum),0)) ELSE SUM(IFNULL(uis.orderAmount/uis.investorPeopleNum,0)) END investorAvgAmount "
			+ " FROM t_user_invest_statistics uis "
			+ " WHERE uis.sortTime BETWEEN ?1 AND ?2 "
			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1, uis.channelOid = ?4) "
			+ " GROUP BY DATE_FORMAT(uis.sortTime,'%Y-%m') "
			+ " ORDER BY uis.sortTime DESC "
			+ " LIMIT ?5,?6 ",nativeQuery=true)
	public List<Object[]> getUserInvestBehaviorByMonthStatisticsList(
			String startTime,String endTime,int isExperGold,String channelOid,
			int pageRow, int row);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getPageCountMonth
	 * @Description:按月统计总条数
	 * @return int
	 * @date 2017年5月8日 上午11:27:48
	 * @since  1.0.0
	 */
	@Query(value=" SELECT COUNT(1) FROM (SELECT COUNT(1) FROM t_user_invest_statistics uis WHERE uis.sortTime BETWEEN ?1 AND ?2 AND IF(?3 IS NULL OR ?3 = '', 1=1, uis.channelOid = ?3) GROUP BY DATE_FORMAT(uis.sortTime,'%Y-%m')) countStat ",nativeQuery=true)
	public int getPageCountMonth(String startTime, String endTime, String channelOid);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryUserRegisterChannelOid
	 * @Description: 用户注册渠道列表
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年6月27日 下午11:58:46
	 * @since  1.0.0
	 */
	@Query(value = " SELECT DISTINCT uis.channelOid FROM t_user_invest_statistics uis ", nativeQuery=true)
	public List<Object[]> queryUserRegisterChannelOid();
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: initUserBehaviorStatistics
	 * @Description: 初始化昨日注册，绑卡，投资统计
	 * @param investDate
	 * @return void
	 * @date 2017年6月27日 下午8:24:52
	 * @since  1.0.0
	 */
	@Modifying
	@Query(value = " INSERT INTO t_user_invest_statistics "
			+ " SELECT"
			+ "   REPLACE(UUID(), '-', '') oid, "
			+ "   invest.registerChannelId registerChannelId, "
			+ "   IFNULL(register.registerNum, 0) registerNum, "
			+ "   IFNULL(register.bindCardNum, 0) bindCardNum, "
			+ "   IFNULL(invest.investorPeopleNum, 0) investorPeopleNum, "
			+ "   IFNULL(invest.orderAmount, 0) orderAmount, "
			+ "   IFNULL(invest.investorPenNum, 0) investorPenNum, "
			+ "   IFNULL(expGoldInvest.investorPeopleGoldNum, 0) investorPeopleGoldNum, "
			+ "   IFNULL(expGoldInvest.orderAmountGold, 0) orderAmountGold, "
			+ "   IFNULL(expGoldInvest.investorPenGoldNum, 0) investorPenGoldNum, "
			+ "   IFNULL(invest.sortTime, 0) sortTime, "
			+ "   SYSDATE() createTime "
			+ " FROM ( "
			+ "     SELECT "
			+ "       mib.registerChannelId registerChannelId, "
			+ "       DATE_FORMAT(mit.createTime,'%Y-%m-%d') sortTime, "
			+ "       COUNT(DISTINCT mit.investorOid) investorPeopleNum , "
			+ "       SUM(mit.orderAmount) orderAmount, "
			+ "       COUNT(1) investorPenNum "
			+ "     FROM t_money_investor_baseaccount mib "
			+ "     LEFT JOIN t_money_investor_tradeorder mit ON mib.oid = mit.investorOid "
			+ "     WHERE mit.orderStatus IN ('confirmed','accepted','paySuccess') AND mit.orderType IN ('invest','noPayInvest') "
			+ "     AND mib.registerChannelId IS NOT NULL AND mib.registerChannelId <> '' "
			+ "     AND DATE_FORMAT(mit.createTime,'%Y-%m-%d') = DATE_FORMAT(?1,'%Y-%m-%d') "
			+ "     GROUP BY mib.registerChannelId, DATE_FORMAT(mit.createTime,'%Y-%m-%d') "
			+ "     ORDER BY DATE_FORMAT(mit.createTime,'%Y-%m-%d') "
			+ " ) invest LEFT JOIN ( "
			+ "     SELECT "
			+ "       mib.registerChannelId registerChannelId, "
			+ "       DATE_FORMAT(mit.createTime,'%Y-%m-%d') sortTime, "
			+ "       COUNT(DISTINCT mit.investorOid) investorPeopleGoldNum , "
			+ "       SUM(mit.orderAmount) orderAmountGold, "
			+ "       COUNT(1) investorPenGoldNum "
			+ "     FROM t_money_investor_baseaccount mib "
			+ "     LEFT JOIN t_money_investor_tradeorder mit ON mib.oid = mit.investorOid "
			+ "     WHERE mit.orderStatus IN ('confirmed','accepted','paySuccess') AND mit.orderType = 'expGoldInvest' "
			+ "     AND mib.registerChannelId IS NOT NULL AND mib.registerChannelId <> '' "
			+ "     AND DATE_FORMAT(mit.createTime,'%Y-%m-%d') = DATE_FORMAT(?1,'%Y-%m-%d') "
			+ "     GROUP BY mib.registerChannelId, DATE_FORMAT(mit.createTime,'%Y-%m-%d') "
			+ "     ORDER BY DATE_FORMAT(mit.createTime,'%Y-%m-%d') "
			+ " ) expGoldInvest ON invest.registerChannelId = expGoldInvest.registerChannelId AND invest.sortTime = expGoldInvest.sortTime "
			+ " LEFT JOIN ( "
			+ "     SELECT "
			+ "       mib.registerChannelId registerChannelId, "
			+ "       DATE_FORMAT(mib.createTime,'%Y-%m-%d') sortTime, "
			+ "       COUNT(1) registerNum, "
			+ "       COUNT(mib.realName) bindCardNum "
			+ "     FROM t_money_investor_baseaccount mib "
			+ "     WHERE mib.registerChannelId IS NOT NULL AND mib.registerChannelId <> '' "
			+ "     AND DATE_FORMAT(mib.createTime,'%Y-%m-%d') = DATE_FORMAT(?1,'%Y-%m-%d') "
			+ "     GROUP BY mib.registerChannelId, DATE_FORMAT(mib.createTime,'%Y-%m-%d') "
			+ "     ORDER BY DATE_FORMAT(mib.createTime,'%Y-%m-%d') "
			+ " ) register ON invest.registerChannelId = register.registerChannelId AND invest.sortTime = register.sortTime ", nativeQuery=true)
	public int initUserBehaviorStatistics(Date investDate);

}
