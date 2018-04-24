package com.guohuai.ams.guess;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface GuessDao  extends JpaRepository<GuessEntity, String>, JpaSpecificationExecutor<GuessEntity>{
	
	/**
	* <p>Title: </p>
	* <p>Description: 优化sql</p>
	* <p>Company: </p> 
	* @return
	* @author 邱亮
	* @date 2017年7月8日 下午6:19:48
	* @since 1.0.0
	*/
//	@Query(value = "SELECT g.guessName,g.guessTitle,g.imgPath,g.status,g.oid FROM T_GAM_GUESS g "
//	+ "INNER JOIN T_GAM_PRODUCT_PACKAGE pp ON(g.oid = pp.guessOid) "
//	+ "INNER JOIN T_GAM_PRODUCT p ON(pp.oid=p.packageOid) "
//	+ "WHERE g.delFlag=0  GROUP BY g.oid ORDER BY pp.raiseStartDate DESC"
//	,nativeQuery= true)
	@Query(value = "SELECT g.guessName,g.guessTitle,g.imgPath,g.status,g.oid FROM T_GAM_GUESS g "
			+ "INNER JOIN T_GAM_PRODUCT_PACKAGE pp ON(g.oid = pp.guessOid) "
			+ "WHERE g.delFlag=0 AND pp.isDeleted = 'NO' ORDER BY pp.raiseStartDate DESC"
			,nativeQuery= true)
	List<Object[]> findAllOrderByRaiseStartDate();
	
	@Modifying
	@Query(value = "update T_GAM_GUESS set status = ?3 where oid = ?1 and status = ?2",nativeQuery= true)
	int updateStatus(String oid,Integer from, Integer to);
	
	@Query(value = "from GuessEntity where status=?1 and delFlag=0")
	List<GuessEntity> findByStatus(Integer status);
	
	@Query(value = "from GuessEntity where delFlag=0")
	List<GuessEntity> findAllNotDel();

	@Query(value = "SELECT e.`guessTitle`,d.`name`,d.`expAror`,c.`orderAmount`,c.`createTime`,d.`repaydate`,b.`content`,b.`percent`,d.`repayLoanStatus` FROM t_money_guess_invest_item a "
			+ "LEFT JOIN t_gam_guess_item b ON(a.`itemOid`=b.`oid`) "
			+ "LEFT JOIN t_gam_guess e ON(b.`guessOid`=e.`oid`) "
			+ "LEFT JOIN t_money_investor_tradeorder c ON(a.`orderOid`=c.`oid`) "
			+ "LEFT JOIN t_gam_product d ON(c.`productOid`=d.`oid`) "
			+ "WHERE c.`investorOid`= ?1 AND c.`orderStatus`='confirmed' AND c.`holdStatus`!='refunded' order by c.createTime desc",nativeQuery= true)
	List<Object[]> findGuessInfoByUid(String uid);
	
	@Query(value = " SELECT mit.orderCode, mit.orderAmount, mit.orderStatus, mit.orderTime, gp.name, gg.guessName, gi.content,gp.raiseEndDate,gi.percent,mit.totalRewardIncome,mib.userOid, mib.realName, mib.phoneNum"
			+ " FROM t_money_investor_tradeorder mit, t_gam_guess gg, t_gam_guess_item gi, t_money_guess_invest_item gii, t_gam_product gp, t_money_investor_baseaccount mib"
			+ " where mit.oid = gii.orderOid and gii.itemOid = gi.Oid and gii.investOid = mib.Oid and mit.productOid = gp.Oid and gg.Oid = gi.guessOid"
			+ " AND IF(?1 IS NULL OR ?1 = '', 1=1, gg.guessName like CONCAT('%',?1,'%')) "
			+ " AND IF(?2 IS NULL OR ?2 = '', 1=1, gp.name like CONCAT('%',?2,'%')) "
			+ " AND IF(?3 IS NULL OR ?3 = '', 1=1, mib.realName like CONCAT('%',?3,'%')) "
			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1, mib.phoneNum like CONCAT('%',?4,'%')) "
			+ " AND IF(?5 IS NULL OR ?5 = '', 1=1, mit.orderStatus= ?5) ORDER BY mit.orderTime DESC LIMIT ?6, ?7", nativeQuery = true)
	public List<Object[]> getGuessRecordList(String guessName, String productName, String realName,  String phoneNum, String orderStatus,int pageRow, int row);
	@Query(value = " SELECT "
			+ " COUNT(1) "
			+ " FROM t_money_investor_tradeorder mit, t_gam_guess gg, t_gam_guess_item gi, t_money_guess_invest_item gii, t_gam_product gp, t_money_investor_baseaccount mib"
			+ " where mit.oid = gii.orderOid and gii.itemOid = gi.Oid and gii.investOid = mib.Oid and mit.productOid = gp.Oid and gg.Oid = gi.guessOid"
			+ " AND IF(?1 IS NULL OR ?1 = '', 1=1, gg.guessName like CONCAT('%',?1,'%')) "
			+ " AND IF(?2 IS NULL OR ?2 = '', 1=1, gp.name like CONCAT('%',?2,'%')) "
			+ " AND IF(?3 IS NULL OR ?3 = '', 1=1, mib.realName like CONCAT('%',?3,'%')) "
			+ " AND IF(?4 IS NULL OR ?4 = '', 1=1, mib.phoneNum like CONCAT('%',?4,'%')) "
			+ " AND IF(?5 IS NULL OR ?5 = '', 1=1, mit.orderStatus= ?5) ", nativeQuery = true)
	public int getGuessRecordCount(String guessName, String productName, String realName,  String phoneNum, String orderStatus);
}
