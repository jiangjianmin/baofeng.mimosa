//package com.guohuai.mmp.investor.bank.bankhis;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.jpa.repository.Query;
//
//public interface BankHisDao extends JpaRepository<BankHisEntity, String>, JpaSpecificationExecutor<BankHisEntity> {
//
//	@Query("FROM BankHisEntity WHERE investorOid = ?1")
//	public List<BankHisEntity> findByInvestorOid(String investorOid);
//}
