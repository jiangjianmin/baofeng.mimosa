package com.guohuai.mmp.investor.bank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;

public interface BankDao extends JpaRepository<BankEntity, String>, JpaSpecificationExecutor<BankEntity> {

	public BankEntity findByInvestorBaseAccount(InvestorBaseAccountEntity baseAccount);
	
	public BankEntity findByIdCard(String idNumb);
	
	public BankEntity findByDebitCard(String debitCard);
}
