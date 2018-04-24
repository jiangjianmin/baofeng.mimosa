package com.guohuai.mmp.publisher.investor.holdapartincome;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PartIncomeDao extends JpaRepository<PartIncomeEntity, String>, JpaSpecificationExecutor<PartIncomeEntity>{

	
	
	/** 我的产品某阶段收益明细信息（持有份额，投资份额，创建时间，单位净值，升档日期天数） */
	@Query(value = " SELECT C.holdVolume,C.investVolume,C.createTime,D.netUnitShare,E.startDate,E.oid "
					+" FROM T_MONEY_INVESTOR_BASEACCOUNT A  "
					+" INNER JOIN T_MONEY_PUBLISHER_INVESTOR_INCOME B ON A.oid = B.investorOid  "
					+" INNER JOIN T_MONEY_PUBLISHER_HOLDAPART C ON B.holdApartOid=C.oid "
					+" INNER JOIN T_GAM_PRODUCT D ON B.productOid=D.oid "
					+" INNER JOIN T_GAM_INCOME_REWARD E ON B.rewardRuleOid=E.oid "
					+" WHERE A.userOid=?1 AND B.productOid=?2 "
					+" AND B.confirmDate = ( "
					+" SELECT MAX(F2.confirmDate)   "
					+" 	 FROM T_MONEY_INVESTOR_BASEACCOUNT F1   "
					+" 	 INNER JOIN T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME F2 ON F1.oid=F2.investorOid   "
					+" 	 WHERE F1.userOid = ?1 AND F2.productOid = ?2 "
				    +" )  "
					+" AND E.level=?3 "
					+" ORDER BY C.createTime DESC "
					+ " limit ?4,?5 ", nativeQuery = true)
	public List<Object[]> queryHoldApartIncomeAndLevel(String userOid, String productOid, String level,int startLine, int endLine);
	
	/** 查询某奖励阶段的下一阶段开始时间 */
	@Query(value = " SELECT A.startDate " //
			+ " FROM T_GAM_INCOME_REWARD A "//
			+ " WHERE A.productOid=?1 "//
			+ " AND A.startDate>( "//
			+ "  SELECT startDate FROM T_GAM_INCOME_REWARD WHERE oid=?2) "
			+ " ORDER BY A.startDate ASC LIMIT 1 ", nativeQuery = true)
	public String queryNextLevelStartDate(String productOid, String rewardOid);
	
	/** 我的产品某阶段收益明细信息（持有份额，投资份额，创建时间，单位净值，升档日期天数） */
	@Query(value = " SELECT COUNT(*) "
					+" FROM T_MONEY_INVESTOR_BASEACCOUNT A  "
					+" INNER JOIN T_MONEY_PUBLISHER_INVESTOR_INCOME B ON A.oid = B.investorOid  "
					+" INNER JOIN T_MONEY_PUBLISHER_HOLDAPART C ON B.holdApartOid=C.oid "
					+" INNER JOIN T_GAM_PRODUCT D ON B.productOid=D.oid "
					+" INNER JOIN T_GAM_INCOME_REWARD E ON B.rewardRuleOid=E.oid "
					+" WHERE A.userOid=?1 AND B.productOid=?2 "
					+" AND B.confirmDate = ( "
					+" SELECT MAX(F2.confirmDate)   "
					+" 	 FROM T_MONEY_INVESTOR_BASEACCOUNT F1   "
					+" 	 INNER JOIN T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME F2 ON F1.oid=F2.investorOid   "
					+" 	 WHERE F1.userOid = ?1 AND F2.productOid = ?2 "
				    +" )  "
					+" AND E.level=?3 ", nativeQuery = true)
	public int counntHoldApartIncomeAndLevel(String userOid, String productOid, String level);
}
