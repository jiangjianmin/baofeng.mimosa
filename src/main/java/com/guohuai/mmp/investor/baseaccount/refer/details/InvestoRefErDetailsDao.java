package com.guohuai.mmp.investor.baseaccount.refer.details;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;

public interface InvestoRefErDetailsDao extends JpaRepository<InvestoRefErDetailsEntity, String>, JpaSpecificationExecutor<InvestoRefErDetailsEntity> {

	public InvestoRefErDetailsEntity findByInvestorBaseAccount(InvestorBaseAccountEntity account);
	
	/**
	 * 推荐排名统计，前10名
	 * @return
	 */
	@Query(value = "SELECT b.phoneNum, b.realName, a.referRegAmount FROM "
			+ " T_MONEY_INVESTOR_BASEACCOUNT_REFEREE a, T_MONEY_INVESTOR_BASEACCOUNT b "
			+ " WHERE a.investorOid = b.oid AND a.referRegAmount > 0 ORDER BY a.referRegAmount DESC LIMIT 10;", nativeQuery = true)
	public List<Object[]> recommendRankTOP10();	
	/**
	 * 根据id 查询被邀请人列表
	 * @return
	 */
	@Query(value = "select  b.realName,b.phoneNum from "
			+ "T_MONEY_INVESTOR_BASEACCOUNT_REFEREE r "
			+ "LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS d "
			+ "on r.oid=d.refereeOid "
			+ "LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT b "
			+ "on d.investorOid=b.oid "
			+ "where r.investorOid=?1 limit ?2,?3", nativeQuery = true)
	public List<Object[]> myReferList(String investorOid,int page,int rows );	
	/**
	 * 根据id 查询被邀请人列表总数
	 * @return
	 */
	@Query(value = "select  count(b.phoneNum) total,COUNT(b.realName >0) realTotal from "
			+ "T_MONEY_INVESTOR_BASEACCOUNT_REFEREE r "
			+ "LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS d "
			+ "on r.oid=d.refereeOid "
			+ "LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT b "
			+ "on d.investorOid=b.oid "
			+ "where r.investorOid=?1  ", nativeQuery = true)
	public Object myReferListSumCount(String investorOid);	
	/**
	 * 邀请好友统计
	 * @param refereeOid
	 * @return
	 */
	@Query(value = "SELECT b.phoneNum, b.createTime FROM T_MONEY_INVESTOR_BASEACCOUNT b "
			+ "WHERE b.oid IN (SELECT s.investorOid FROM T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS s "
			+ "WHERE s.refereeOid = ?1) ", nativeQuery = true)
	public List<Object[]> getRecPeopleInfo(String refereeOid);
	
	/**
	 * 邀请好友绑卡统计
	 * @param refereeOid
	 * @return
	 */
	@Query(value = "SELECT b.phoneNum, b.updateTime FROM T_MONEY_INVESTOR_BASEACCOUNT b "
			+ "WHERE b.oid IN (SELECT s.investorOid FROM T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS s "
			+ "WHERE s.refereeOid = ?1) "
			+ "AND b.realName IS NOT NULL AND b.idNum IS NOT NULL order by b.oid", nativeQuery = true)
	public List<Object[]> getRecBindBankPeopleInfo(String refereeOid);
	
	@Query(value = "SELECT * FROM T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS a WHERE a.investorOid = ?1 LIMIT 1;", nativeQuery = true)
	public InvestoRefErDetailsEntity getRefErDetailsByInvestorOid(String investorOid);
	
	@Query(value = " SELECT "
			+ " DISTINCT "
			+ " b.phoneNum, "
			+ " b.createTime, "
			+ " IF(b.realName IS NOT NULL,b.realName,'-') realName, "
			+ " IF(b.realName IS NOT NULL AND b.idNum IS NOT NULL,'是','否') isBankStr, "
			+ " IF(mit.orderCode IS NOT NULL,'是','否') isInvestor "
			+ " FROM t_money_investor_baseaccount_referee referee "
			+ " LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS d ON referee.oid = d.refereeOid "
			+ " LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT b ON d.investorOid = b.oid "
			+ " LEFT JOIN t_money_investor_tradeorder mit ON b.oid = mit.investorOid AND mit.orderType = 'invest' AND mit.orderStatus IN ('paySuccess','accepted','confirmed') "
			+ " WHERE referee.investorOid = ?1 "
			+ " LIMIT ?2,?3 ", nativeQuery = true)
	public List<Object[]> queryRecommendInfo(String userOid, int i, int row);
	
	@Query(value = " SELECT"
			+ " COUNT(1) "
			+ " FROM t_money_investor_baseaccount_referee referee "
			+ " LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT_REFER_DETAILS d ON referee.oid = d.refereeOid "
			+ " LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT b ON d.investorOid = b.oid "
			+ " WHERE referee.investorOid = ?1 ", nativeQuery = true)
	public int queryRecommendInfoCount(String userOid);
	
	@Query(value = " SELECT "
			+ " CONCAT(SUBSTRING(mib.phoneNum,1,3),'****',SUBSTRING(mib.phoneNum,8,4)) phoneNum, "
			+ " DATE_FORMAT(mit.orderTime, '%Y-%m-%d %H:%m:%s') orderTime "
			+ " FROM t_money_investor_baseaccount_referee referee "
			+ " LEFT JOIN t_money_investor_baseaccount_refer_details mid ON referee.oid = mid.refereeOid "
			+ " LEFT JOIN t_money_investor_tradeorder mit ON mid.investorOid = mit.investorOid "
			+ " LEFT JOIN t_money_investor_baseaccount mib ON mid.investorOid = mib.oid "
			+ " WHERE referee.investorOid = ?1 "
			+ " AND mit.orderType = 'invest' AND mit.orderStatus IN ('paySuccess','accepted','confirmed') "
			+ " ORDER BY mit.createTime DESC "
			+ " LIMIT ?2,?3 ", nativeQuery = true)
	public List<Object[]> queryFriendInvestRecord(String userOid, int i, int row);
	
	@Query(value = " SELECT "
			+ " COUNT(1) "
			+ " FROM t_money_investor_baseaccount_referee referee "
			+ " LEFT JOIN t_money_investor_baseaccount_refer_details mid ON referee.oid = mid.refereeOid "
			+ " LEFT JOIN t_money_investor_tradeorder mit ON mid.investorOid = mit.investorOid "
			+ " LEFT JOIN t_money_investor_baseaccount mib ON mid.investorOid = mib.oid "
			+ " WHERE referee.investorOid = ?1 "
			+ " AND mit.orderType = 'invest' AND mit.orderStatus IN ('paySuccess','accepted','confirmed') ", nativeQuery = true)
	public int queryFriendInvestRecordCount(String userOid);
	
	@Query(value = " SELECT "
			+ " COUNT(1) registNum, "
			+ " COUNT(b.idNum) bindBankNum "
			+ " FROM t_money_investor_baseaccount_referee referee "
			+ " LEFT JOIN t_money_investor_baseaccount_refer_details d ON referee.oid = d.refereeOid "
			+ " LEFT JOIN T_MONEY_INVESTOR_BASEACCOUNT b ON d.investorOid = b.oid "
			+ " WHERE referee.investorOid = ?1 ", nativeQuery = true)
	public List<Object[]> queryRecommendStat(String userOid);
}
