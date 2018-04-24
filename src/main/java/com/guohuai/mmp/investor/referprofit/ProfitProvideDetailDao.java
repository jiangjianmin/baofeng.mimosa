package com.guohuai.mmp.investor.referprofit;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProfitProvideDetailDao extends JpaRepository<ProfitProvideDetailEntity, String>,JpaSpecificationExecutor<ProfitProvideDetailEntity> {
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProfitProvideDetailSum
	 * @Description: 汇总查询二级奖励收益明细，按投资人分发奖励收益(未发放)
	 * @return List<Object[]>
	 * @date 2017年6月14日 下午4:01:20
	 * @since  1.0.0
	 */
	@Query(value = " SELECT provideOid, SUM(IFNULL(provideAmount,0)) provideAmount, provideMonth "
			+ " FROM t_money_profit_provide_detail "
			+ " WHERE provideMonth = ?1 AND status = 'toClose' "
			+ " GROUP BY provideOid ", nativeQuery = true)
	List<Object[]> getProfitProvideDetailSum(String lastYearMonth);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: updateProfitProvideDetailStatus
	 * @Description: 更新发放状态
	 * @param provideOid
	 * @param provideMonth
	 * @return int
	 * @date 2017年6月14日 下午6:17:27
	 * @since  1.0.0
	 */
	@Modifying
	@Query(value = " UPDATE t_money_profit_provide_detail SET provideDate = SYSDATE(), status = 'closed' "
			+ " WHERE provideOid = ?1 AND provideMonth = ?2 AND status = 'toClose' ", nativeQuery = true)
	int updateProfitProvideDetailStatus(String provideOid, String provideMonth);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProfitProvideDetailStat
	 * @Description: 奖励汇总查询
	 * @param uid
	 * @return List<Object[]>
	 * @date 2017年6月15日 下午2:44:57
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " TRUNCATE(SUM(sta.toPayAmount+sta.payAmount),2) totalAmount, "
			+ " TRUNCATE(SUM(sta.toPayAmount),2) toPayAmount, "
			+ " TRUNCATE(SUM(sta.payAmount),2) payAmount "
			+ " FROM ( "
			+ "   SELECT "
			+ "     CASE WHEN status = 'toClose' THEN provideAmount ELSE 0 END toPayAmount, "
			+ "     CASE WHEN status = 'closed' THEN provideAmount ELSE 0 END payAmount "
			+ "   FROM t_money_profit_provide_detail "
			+ "   WHERE provideOid = ?1 "
			+ " ) sta ", nativeQuery = true)
	List<Object[]> getProfitProvideDetailStat(String uid);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProfitProvideDetailList
	 * @Description: 查询奖励详情列表
	 * @param uid
	 * @param status
	 * @param payDate
	 * @param pageRow
	 * @param row
	 * @return List<Object[]>
	 * @date 2017年6月15日 下午2:45:10
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " sourcePhone, "
			+ " payDate, "
			+ " productName, "
			+ " TRUNCATE(provideAmount,2) provideAmount "
			+ " FROM t_money_profit_provide_detail "
			+ " WHERE provideOid = ?1 "
			+ " AND (CASE WHEN ?2=0 THEN STATUS = 'toClose' WHEN ?2=1 THEN STATUS = 'closed' ELSE 1=1 END) "
			+ " AND IF(?3 = -1, 1=1, DATE_FORMAT(createTime, '%Y-%m-%d') > DATE_FORMAT(DATE_ADD(SYSDATE(), INTERVAL -?4 DAY), '%Y-%m-%d') AND DATE_FORMAT(createTime, '%Y-%m-%d') <= DATE_FORMAT(SYSDATE(), '%Y-%m-%d')) "
			+ " ORDER BY createTime DESC "
			+ " LIMIT ?5,?6 ", nativeQuery = true)
	List<Object[]> getProfitProvideDetailList(String uid, int status, int timeSign, int timeDemision, int pageRow, int row);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProfitProvideDetailCount
	 * @Description: 查询奖励详情列表总条数
	 * @param uid
	 * @param status
	 * @param payDate
	 * @return int
	 * @date 2017年6月15日 下午2:46:28
	 * @since  1.0.0
	 */
	@Query(value = " SELECT "
			+ " count(1) "
			+ " FROM t_money_profit_provide_detail "
			+ " WHERE provideOid = ?1 "
			+ " AND ( CASE WHEN ?2 = 0 THEN STATUS = 'toClose' WHEN ?2 = 1 THEN STATUS = 'closed' ELSE 1=1 END ) "
			+ " AND IF(?3 = -1, 1=1, DATE_FORMAT(createTime,'%Y-%m-%d') > DATE_FORMAT(DATE_ADD(SYSDATE(), INTERVAL -?4 DAY), '%Y-%m-%d') AND DATE_FORMAT(createTime, '%Y-%m-%d') <= DATE_FORMAT(SYSDATE(), '%Y-%m-%d')) ", nativeQuery = true)
	int getProfitProvideDetailCount(String uid, int status, int timeSign, int timeDemision);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProfitProvideRankDay
	 * @Description: 查询龙虎榜--昨日榜
	 * @return List<Object[]>
	 * @date 2017年6月15日 下午9:39:35
	 * @since  1.0.0
	 */
	@Query(value = " SELECT phone,provideAmount FROM ( "
			+ " SELECT "
			+ " phone, "
			+ " TRUNCATE(SUM(provideAmount),2) provideAmount "
			+ " FROM t_money_profit_provide_detail "
			+ " WHERE DATE_FORMAT(createTime,'%Y-%m-%d') = ?1 "
			+ " GROUP BY provideOid "
			+ " ) rank ORDER BY provideAmount DESC LIMIT 10 ", nativeQuery = true)
	List<Object[]> getProfitProvideRankDay(String lastDay);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProfitProvideRankWeek
	 * @Description: 查询龙虎榜--上周榜
	 * @return List<Object[]>
	 * @date 2017年6月15日 下午9:40:03
	 * @since  1.0.0
	 */
	@Query(value = " SELECT phone,provideAmount FROM ( "
			+ " SELECT "
			+ " phone, "
			+ " TRUNCATE(SUM(provideAmount),2) provideAmount "
			+ " FROM t_money_profit_provide_detail "
			+ " WHERE DATE_FORMAT(createTime,'%Y-%m-%d') >= ?1 AND DATE_FORMAT(createTime,'%Y-%m-%d') <= ?2 "
			+ " GROUP BY provideOid "
			+ " ) rank ORDER BY provideAmount DESC LIMIT 10 ", nativeQuery = true)
	List<Object[]> getProfitProvideRankWeek(String lastMondayDate, String lastSundayDate);
	/**
	 * 
	 * @author yihonglei
	 * @Title: getProfitProvideRankMonth
	 * @Description: 查询龙虎榜--上月榜
	 * @return List<Object[]>
	 * @date 2017年6月15日 下午9:40:25
	 * @since  1.0.0
	 */
	@Query(value = " SELECT phone,provideAmount FROM ( "
			+ " SELECT "
			+ " phone, "
			+ " TRUNCATE(SUM(provideAmount),2) provideAmount "
			+ " FROM t_money_profit_provide_detail "
			+ " WHERE DATE_FORMAT(createTime,'%Y-%m-%d') >= ?1 AND DATE_FORMAT(createTime,'%Y-%m-%d') <= ?2 "
			+ " GROUP BY provideOid "
			+ " ) rank ORDER BY provideAmount DESC LIMIT 10 ", nativeQuery = true)
	List<Object[]> getProfitProvideRankMonth(String lastMonthStart, String lastMonthEnd);
	/** 投资人奖励发放明细 */
	@Modifying
	@Query(value = " INSERT INTO t_money_profit_provide_detail "
			+ " SELECT "
			+ "   oid,investorOid,investorPhoneNum,sourceOid,sourcePhoneNum,productType,productOid,productName, "
			+ "   orderOid,profitOid,payDate,provideMonth,provideAmount,provideDate,STATUS,createTime "
			+ " FROM ( "
			+ " SELECT "
			+ "   REPLACE(UUID(),'-','') oid, mpd.investorOid investorOid, mibInvestor.phoneNum investorPhoneNum, "
			+ "   mpd.investorOid sourceOid, mibInvestor.phoneNum sourcePhoneNum, gp.type productType, "
			+ "   mpd.productOid productOid, gp.name productName, mpd.orderOid orderOid, mpd.oid profitOid, "
			+ "   mpd.payDate payDate, "
			+ "   CASE WHEN ?5 = 'PRODUCTTYPE_02' THEN DATE_FORMAT(DATE_ADD(SYSDATE(),INTERVAL -1 MONTH),'%Y-%m') ELSE DATE_FORMAT(gp.setupDate,'%Y-%m') END provideMonth, "
			+ "   TRUNCATE(mpd.totalInterest * ?2 * ?3, 2) provideAmount, NULL provideDate, "
			+ "   'toClose' STATUS, SYSDATE() createTime "
			+ " FROM t_money_profit_detail mpd "
			+ " LEFT JOIN t_money_investor_baseaccount mibInvestor ON mpd.investorOid = mibInvestor.oid "
			+ " LEFT JOIN t_gam_product gp ON mpd.productOid = gp.oid "
			+ " WHERE mpd.productOid = ?1 AND IF( ?5 = 'PRODUCTTYPE_01' OR ?5 IS NULL, 1=1, mpd.payDate=DATE_FORMAT(?4,'%Y-%m') ) "
			+ " ) investorProfitDetail WHERE provideAmount > 0 ", nativeQuery = true)
	int proInvestorProfitProvideDetail(String productOid, BigDecimal productFactor, BigDecimal investorFactor, Date t0Date, String productType);
	/** 一级邀请人奖励发放明细 */
	@Modifying
	@Query(value = " INSERT INTO t_money_profit_provide_detail "
			+ " SELECT "
			+ "   oid,firstOid,firstPhoneNum,sourceOid,sourcePhoneNum,productType,productOid,productName, "
			+ "   orderOid,profitOid,payDate,provideMonth,provideAmount,provideDate,STATUS,createTime "
			+ " FROM ( "
			+ " SELECT "
			+ "   REPLACE(UUID(),'-','') oid, mpd.firstOid firstOid, mibFirst.phoneNum firstPhoneNum, "
			+ "   mpd.investorOid sourceOid, sourceInvestor.phoneNum sourcePhoneNum, gp.type productType, "
			+ "   mpd.productOid productOid, gp.name productName, mpd.orderOid orderOid, mpd.oid profitOid, "
			+ "   mpd.payDate payDate, "
			+ "   CASE WHEN ?5 = 'PRODUCTTYPE_02' THEN DATE_FORMAT(DATE_ADD(SYSDATE(),INTERVAL -1 MONTH),'%Y-%m') ELSE DATE_FORMAT(gp.setupDate,'%Y-%m') END provideMonth, "
			+ "   TRUNCATE(mpd.totalInterest * ?2 * ?3, 2) provideAmount, NULL provideDate, "
			+ "   'toClose' STATUS, SYSDATE() createTime "
			+ " FROM t_money_profit_detail mpd "
			+ " LEFT JOIN t_money_investor_baseaccount mibFirst ON mpd.firstOid = mibFirst.oid "
			+ " LEFT JOIN t_money_investor_baseaccount sourceInvestor ON mpd.investorOid = sourceInvestor.oid "
			+ " LEFT JOIN t_gam_product gp ON mpd.productOid = gp.oid "
			+ " WHERE mpd.investorOid IS NOT NULL AND mpd.firstOid IS NOT NULL AND mpd.productOid = ?1 AND IF( ?5 = 'PRODUCTTYPE_01' OR ?5 IS NULL, 1=1, mpd.payDate=DATE_FORMAT(?4,'%Y-%m') ) "
			+ " ) investorProfitDetail WHERE provideAmount > 0 ", nativeQuery = true)
	int proFirstProfitProvideDetail(String productOid, BigDecimal productFactor, BigDecimal firstFactor, Date t0Date, String productType);
	
	/** 二级邀请人奖励发放明细 */
	@Modifying
	@Query(value = " INSERT INTO t_money_profit_provide_detail "
			+ " SELECT "
			+ "   oid,secondOid,secondPhoneNum,sourceOid,sourcePhoneNum,productType,productOid,productName, "
			+ "   orderOid,profitOid,payDate,provideMonth,provideAmount,provideDate,STATUS,createTime "
			+ " FROM ( "
			+ " SELECT "
			+ "   REPLACE(UUID(),'-','') oid, mpd.secondOid secondOid, mibSecond.phoneNum secondPhoneNum, "
			+ "   mpd.investorOid sourceOid, sourceInvestor.phoneNum sourcePhoneNum, gp.type productType, "
			+ "   mpd.productOid productOid, gp.name productName, mpd.orderOid orderOid, mpd.oid profitOid, "
			+ "   mpd.payDate payDate, "
			+ "   CASE WHEN ?5 = 'PRODUCTTYPE_02' THEN DATE_FORMAT(DATE_ADD(SYSDATE(),INTERVAL -1 MONTH),'%Y-%m') ELSE DATE_FORMAT(gp.setupDate,'%Y-%m') END provideMonth, "
			+ "   TRUNCATE(mpd.totalInterest * ?2 * ?3, 2) provideAmount, NULL provideDate, "
			+ "   'toClose' STATUS, SYSDATE() createTime "
			+ " FROM t_money_profit_detail mpd "
			+ " LEFT JOIN t_money_investor_baseaccount mibSecond ON mpd.secondOid = mibSecond.oid "
			+ " LEFT JOIN t_money_investor_baseaccount sourceInvestor ON mpd.investorOid = sourceInvestor.oid "
			+ " LEFT JOIN t_gam_product gp ON mpd.productOid = gp.oid "
			+ " WHERE mpd.investorOid IS NOT NULL AND mpd.secondOid IS NOT NULL AND mpd.productOid = ?1 AND IF( ?5 = 'PRODUCTTYPE_01' OR ?5 IS NULL, 1=1, mpd.payDate=DATE_FORMAT(?4,'%Y-%m') ) "
			+ " ) investorProfitDetail WHERE provideAmount > 0 ", nativeQuery = true)
	int proSecondProfitProvideDetail(String productOid, BigDecimal productFactor, BigDecimal secondFactor, Date t0Date, String productType);
	
}
