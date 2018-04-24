package com.guohuai.mmp.investor.referprofit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProfitDetailDao extends JpaRepository<ProfitDetailEntity, String>,JpaSpecificationExecutor<ProfitDetailEntity> {
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: initProductProfitDetail
	 * @Description: 生成定期奖励收益明细
	 * @param oid
	 * @return int
	 * @date 2017年6月13日 下午4:13:13
	 * @since  1.0.0
	 */
	@Modifying
	@Query(value = " INSERT INTO t_money_profit_detail "
			+ " SELECT "
			+ " REPLACE(UUID(),'-','') oid, mit.investorOid investorOid, refereeParent.investorOid firstOid, refereeGrandFather.investorOid secondOid, mit.productOid productOid, mit.oid orderOid, gp.type productType, "
			+ " DATE_FORMAT(mit.orderTime,'%Y-%m-%d') payDate, TRUNCATE((mit.orderAmount*expAror/incomeCalcBasis)*durationPeriodDays+IFNULL(mpr.incomeAmount,0),4) totalInterest, SYSDATE() createTime "
			+ " FROM t_money_investor_tradeorder mit LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
			+ " LEFT JOIN t_money_investor_baseaccount_refer_details parent ON mit.investorOid = parent.investorOid AND DATE_FORMAT(parent.createTime,'%Y-%m-%d') >= ?2 "
			+ " LEFT JOIN t_money_investor_baseaccount_referee refereeParent ON parent.refereeOid = refereeParent.oid "
			+ " LEFT JOIN t_money_investor_baseaccount_refer_details grandFather ON refereeParent.investorOid = grandFather.investorOid AND DATE_FORMAT(grandFather.createTime,'%Y-%m-%d') >= ?2 "
			+ " LEFT JOIN t_money_investor_baseaccount_referee refereeGrandFather ON grandFather.refereeOid = refereeGrandFather.oid "
			+ " LEFT JOIN t_money_product_raising_income mpr ON mit.oid = mpr.orderOid "
			+ " WHERE mit.productOid = ?1 AND "
			+ " parent.refereeOid IS NOT NULL AND refereeParent.oid IS NOT NULL AND mit.orderType = 'invest' AND mit.orderStatus = 'confirmed' ", nativeQuery = true)
	int initTnProductProfitDetail(String productOid, String secondLevelInvestStartDate);
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: initT0ProductProfitDetail
	 * @Description: 生成上月活期奖励收益明细
	 * @param productOid
	 * @return int
	 * @date 2017年6月14日 上午11:06:19
	 * @since  1.0.0
	 */
	@Modifying
	@Query(value = " INSERT INTO t_money_profit_detail "
			+ " SELECT "
			+ " REPLACE(UUID(),'-','') oid, investor.investorOid investorOid, refereeParent.investorOid firstOid, refereeGrandFather.investorOid secondOid, investor.productOid productOid, "
			+ " NULL orderOid, gp.type productType, ?2 payDate, investor.totalInterest totalInterest, SYSDATE() createTime "
			+ " FROM ( "
			+ "   SELECT investorOid, SUM(baseAmount) totalInterest, productOid FROM t_money_publisher_investor_income "
			+ "   WHERE productOid=?1 AND confirmDate LIKE CONCAT(?2,'%') "
			+ "   GROUP BY investorOid "
			+ " ) investor "
			+ " LEFT JOIN t_gam_product gp ON investor.productOid = gp.oid "
			+ " LEFT JOIN t_money_investor_baseaccount_refer_details parent ON investor.investorOid=parent.investorOid AND DATE_FORMAT(parent.createTime,'%Y-%m-%d') >= ?3 "
			+ " LEFT JOIN t_money_investor_baseaccount_referee refereeParent ON parent.refereeOid = refereeParent.oid "
			+ " LEFT JOIN t_money_investor_baseaccount_refer_details grandFather ON refereeParent.investorOid = grandFather.investorOid AND DATE_FORMAT(grandFather.createTime,'%Y-%m-%d') >= ?3 "
			+ " LEFT JOIN t_money_investor_baseaccount_referee refereeGrandFather ON grandFather.refereeOid = refereeGrandFather.oid "
			+ " WHERE parent.refereeOid IS NOT NULL AND refereeParent.oid IS NOT NULL ", nativeQuery = true)
	int initT0ProductProfitDetail(String productOid, String lastYearMonth, String secondLevelInvestStartDate);
	
	@Query(value = " SELECT COUNT(1) FROM t_money_profit_detail WHERE payDate = ?1 AND productOid = ?2 ",  nativeQuery = true)
	int queryT0ProductProfitCount(String lastYearMonth, String productOid);
	
}
