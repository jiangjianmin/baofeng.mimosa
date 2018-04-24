package com.guohuai.mmp.investor.referprofit;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProfitProvideStatisticsDao extends JpaRepository<ProfitProvideRecordEntity, String>,JpaSpecificationExecutor<ProfitProvideRecordEntity> {

	/**
	 * 二级邀请后台统计-收益明细
	 * @author yangzengsen
	 * @param userName
	 * @param inviteType
	 * @param productType
	 * @param productShortName
	 * @param rewardMonthBegin
	 * @param rewardMonthEnd
	 * @param createTimeBegin
	 * @param createTimeEnd
	 * @param pageRow
	 * @param row
	 * @return
	 */
	@Query(value = " select "
			+ " a.oid,a.createTime,a.totalInterest,c.orderCode,b.`name` productName,a.productType, "
			+ " d.realName investorName,e.realName firstInvestorName,f.realName secondInvestorName "
			+ " from t_money_profit_detail a "
			+ " left join t_gam_product b on a.productOid = b.oid "
			+ " left join t_money_investor_tradeorder c on a.orderOid = c.oid "
			+ " left join t_money_investor_baseaccount d on a.investorOid = d.userOid "
			+ " left join t_money_investor_baseaccount e on a.firstOid = e.userOid "
			+ " left join t_money_investor_baseaccount f on a.secondOid = f.userOid "
			+ " where 1=1 "
			+ " and IF( ?3 = '' OR ?3 IS NULL,1=1,a.productType=?3 ) "
			+ " and IF( ?4 = '' OR ?4 IS NULL,1=1,b.name like %?4% ) "
			+ " and IF( ?5 = '' OR ?5 IS NULL,1=1,DATE_FORMAT(a.createTime,'%Y-%m-%d') between ?5 and ?6) "
			+ " and case when ((?2 = '' or ?2 is null) and (?1 = '' or ?1 is null)) then 1=1 "
			+ " when ((?2 = '' or ?2 is null) and ?1 is not null) then (d.realName = ?1 or e.realName = ?1 or f.realName = ?1) "
			+ " when ?2 = 'investor' then d.realName = ?1 "
			+ " when ?2 = 'firstInvitor' then e.realName = ?1 "
			+ " when ?2 = 'secondInvitor' then f.realName = ?1 "
			+ " ELSE 1=1 END "
			+ " LIMIT ?7,?8 " , nativeQuery = true)
	public List<Object[]> getProfitDetailStatisticsList(
			String userName,String inviteType,String productType,String productShortName,Date createTimeBegin,Date createTimeEnd,
			int pageRow, int row);
	
	@Query(value = " select count(1)"
			+ " from t_money_profit_detail a "
			+ " left join t_gam_product b on a.productOid = b.oid "
			+ " left join t_money_investor_tradeorder c on a.orderOid = c.oid "
			+ " left join t_money_investor_baseaccount d on a.investorOid = d.userOid "
			+ " left join t_money_investor_baseaccount e on a.firstOid = e.userOid "
			+ " left join t_money_investor_baseaccount f on a.secondOid = f.userOid "
			+ " where 1=1 "
			+ " and IF( ?3 = '' OR ?3 IS NULL,1=1,a.productType=?3 ) "
			+ " and IF( ?4 = '' OR ?4 IS NULL,1=1,b.name like %?4% ) "
			+ " and IF( ?5 = '' OR ?5 IS NULL,1=1,DATE_FORMAT(a.createTime,'%Y-%m-%d') between ?5 and ?6) "
			+ " and case when ?2 = '' or ?2 is null then 1=1 "
			+ " when ?2 = 'investor' then d.realName = ?1 "
			+ " when ?2 = 'firstInvitor' then e.realName = ?1 "
			+ " when ?2 = 'secondInvitor' then f.realName = ?1 "
			+ " ELSE 1=1 END " , nativeQuery = true)
	public int getPageCountProfitDetailStatistics(String userName,String inviteType,String productType,String productShortName,Date createTimeBegin,Date createTimeEnd);
	
	/**
	 * 邀请奖励-奖励发放明细
	 * @param userName
	 * @param provideRewardStatus
	 * @param productType
	 * @param productShortName
	 * @param rewardMonthBegin
	 * @param rewardMonthEnd
	 * @param createTimeBegin
	 * @param createTimeEnd
	 * @param pageRow
	 * @param row
	 * @return
	 */
	@Query(value = "select "
			+ " a.oid,a.createTime,a.provideAmount, "
			+ " b.realName provideUserName,c.realName sourceProvideName, DATE_FORMAT(a.provideDate,'%Y-%m-%d') provideDate, "
			+ " a.`status`,a.profitOid, d.orderCode,a.payDate,a.productName,a.productType,a.sourceOid,a.provideOid "
			+ " from t_money_profit_provide_detail a "
			+ " left join t_money_investor_baseaccount b on a.provideOid = b.userOid "
			+ " left join t_money_investor_baseaccount c on a.sourceOid = c.userOid "
			+ " left join t_money_investor_tradeorder d on a.orderOid = d.oid "
			+ " where 1=1 "
			+ " and IF( ?1 = '' OR ?1 IS NULL,1=1,b.realName like %?1% ) "
			+ " and IF( ?2 = '' OR ?2 IS NULL,1=1,a.status=?2 ) "
			+ " and IF( ?3 = '' OR ?3 IS NULL,1=1,a.productType=?3 ) "
			+ " and IF( ?4 = '' OR ?4 IS NULL,1=1,a.productName=?4 ) "
			+ " and IF( ?5 = '' OR ?5 IS NULL,1=1,DATE_FORMAT(a.createTime,'%Y-%m-%d') between ?5 and ?6 ) "
			+ " LIMIT ?7,?8 " , nativeQuery = true)
	public List<Object[]> getProfitProvideDetailStatisticsList(
			String userName,String provideRewardStatus,String productType,String productShortName,Date provideTimeBegin,Date provideTimeEnd,
			int pageRow, int row);
	
	@Query(value = "select count(1)"
			+ " from t_money_profit_provide_detail a "
			+ " left join t_money_investor_baseaccount b on a.provideOid = b.userOid "
			+ " left join t_money_investor_baseaccount c on a.sourceOid = c.userOid "
			+ " left join t_money_investor_tradeorder d on a.orderOid = d.oid "
			+ " where 1=1 "
			+ " and IF( ?1 = '' OR ?1 IS NULL,1=1,b.realName like %?1% ) "
			+ " and IF( ?2 = '' OR ?2 IS NULL,1=1,a.status=?2 ) "
			+ " and IF( ?3 = '' OR ?3 IS NULL,1=1,a.productType=?3 ) "
			+ " and IF( ?4 = '' OR ?4 IS NULL,1=1,a.productName=?4 ) "
			+ " and IF( ?5 = '' OR ?5 IS NULL,1=1,DATE_FORMAT(a.createTime,'%Y-%m-%d') between ?5 and ?6 ) " , nativeQuery = true)
	public int getPageCountProfitProvideDetail(String userName,String provideRewardStatus,String productType,String productShortName,Date provideTimeBegin,Date provideTimeEnd);
	
	/**
	 * 当前已发放总金额
	 * @return
	 */
	@Query(value = " select sum(provideAmount) providedTotalAmout from t_money_profit_provide_detail where status = 'closed' " , nativeQuery = true)
	public BigDecimal getProvidedTotalAmout();
	
	/**
	 * 当前待发放总金额
	 * @return
	 */
	@Query(value = " select sum(provideAmount) toProvideTotalAmout from t_money_profit_provide_detail where status = 'toClose' " , nativeQuery = true)
	public BigDecimal getToProvideTotalAmout();
	
	/**
	 * 平台累计奖励人数
	 * @return
	 */
	@Query(value = " select count(DISTINCT(provideOid)) providedTotalNums from t_money_profit_provide_detail " , nativeQuery = true)
	public int getProvidedTotalNums();
	
}
