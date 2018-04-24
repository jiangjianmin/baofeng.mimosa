package com.guohuai.mmp.publisher.investor.levelincome;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LevelIncomeDao extends JpaRepository<LevelIncomeEntity, String>, JpaSpecificationExecutor<LevelIncomeEntity> {
	
	
	@Query(value = "select * from T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME where investorOid = ?1 and productOid = ?2 and confirmDate = ?3", nativeQuery = true)
	List<LevelIncomeEntity> queryBalanceGroupByLevel(String investorOid, String productOid, String confirmDate);

	/** 查询投资人某产品的最近一天的分档明细（奖励收益率，起始日期，结束日期，最新市值，累计金额，阶段） */
	@Query(value = "SELECT A.ratio,A.startDate,A.endDate,D.value,D.incomeAmount,A.level "
				+" FROM T_GAM_INCOME_REWARD A "
				+" LEFT JOIN ( "
				+"    SELECT B.value,B.incomeAmount,B.rewardRuleOid "
				+"    FROM T_MONEY_INVESTOR_BASEACCOUNT C "
				+"    INNER JOIN T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME B ON C.oid = B.investorOid  "
				+"    WHERE C.userOid = ?1 AND B.productOid = ?2 "
				+"    AND B.confirmDate =   "
				+"    ( SELECT MAX(F2.confirmDate)  "
				+" 	    FROM T_MONEY_INVESTOR_BASEACCOUNT F1  "
				+"      INNER JOIN T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME F2 ON F1.oid=F2.investorOid " 
				+"      WHERE F1.userOid = ?1 AND F2.productOid = ?2 "
				+"    ) "
				+"  )D ON A.oid=D.rewardRuleOid "
				+" WHERE A.productOid = ?2  "
				+" ORDER BY A.startDate ASC", nativeQuery = true)
	List<Object[]> queryLevelIncome(String userOid,String productOid);

}
