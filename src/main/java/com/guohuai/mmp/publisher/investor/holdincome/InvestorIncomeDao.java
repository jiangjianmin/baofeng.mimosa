package com.guohuai.mmp.publisher.investor.holdincome;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface InvestorIncomeDao extends JpaRepository<InvestorIncomeEntity, String>, JpaSpecificationExecutor<InvestorIncomeEntity>{
	
	@Query(value = "select * from T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME where investorOid = ?1 and confirmDate = ?2", nativeQuery = true)
	List<InvestorIncomeEntity> findByInvestorOidAndConfirmDate(String investorOid, String incomeDate);
	

	
	@Query(value = "select * from T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME where investorOid =  ?1 "
			+ " and date_format(confirmDate, '%Y%m') = ?2 order by confirmDate", nativeQuery = true)
	List<InvestorIncomeEntity> queryIncomeByYearMonth(String investorOid, String yearMonth);

	@Query(value = "select * from T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME where investorOid =  ?1 and productOid in ?2"
			+ " order by confirmDate desc limit ?3, ?4", nativeQuery = true)
	List<InvestorIncomeEntity> queryIncomeByPage(String investorOid, List<String> productOidList, int beginIndex, int pageSize);
	
	@Query(value = "select count(1) from T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME where investorOid =  ?1 and productOid in ?2", nativeQuery = true)
	int queryCountIncome(String investorOid, List<String> productOidList);

	@Query(value = " SELECT * FROM T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME WHERE productOid = ?1 AND investorOid = ?2 AND confirmDate = ?3",
			nativeQuery = true)
	InvestorIncomeEntity getCycleProductInterest(String productOid, String investorOid, Date confirmDate);
}
