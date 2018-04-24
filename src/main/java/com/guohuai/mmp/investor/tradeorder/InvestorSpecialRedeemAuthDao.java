package com.guohuai.mmp.investor.tradeorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InvestorSpecialRedeemAuthDao extends JpaRepository<InvestorSpecialRedeemAuthEntity, String>, JpaSpecificationExecutor<InvestorSpecialRedeemAuthEntity>{

	@Query("update InvestorSpecialRedeemAuthEntity set operateStatus = ?2  where userId = ?1 and operateStatus = 'toOperate' ")
	@Modifying
	public int updateOperateStatus(String userId,String operateStatus);
	
	public InvestorSpecialRedeemAuthEntity findByUserIdAndOperateStatus(String userId,String operateStatus);
}
