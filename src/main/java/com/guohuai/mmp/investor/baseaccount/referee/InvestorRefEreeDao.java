package com.guohuai.mmp.investor.baseaccount.referee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;

public interface InvestorRefEreeDao extends JpaRepository<InvestorRefEreeEntity, String>, JpaSpecificationExecutor<InvestorRefEreeEntity> {

	/**
	 * 更新注册人数
	 * @param refEreeOid
	 * @return
	 */
	@Query("UPDATE InvestorRefEreeEntity a SET a.referRegAmount = referRegAmount + 1, a.updateTime = sysdate() WHERE a.oid = ?1")
	@Modifying
	public int updateRegister(String refEreeOid);
	
	public InvestorRefEreeEntity findByInvestorBaseAccount(InvestorBaseAccountEntity investorBaseAccount);
	
}
