package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InvestorSpecialRedeemDao extends JpaRepository<InvestorSpecialRedeemEntity, String>, JpaSpecificationExecutor<InvestorSpecialRedeemEntity>{
	
	@Query("update InvestorSpecialRedeemEntity set leftSpecialRedeemAmount = leftSpecialRedeemAmount - ?2  where userId = ?1 and leftSpecialRedeemAmount >= ?2")
	@Modifying
	public int updateLeftSpecialRedeemAmount(String userId,BigDecimal orderVolume);
	
	public InvestorSpecialRedeemEntity findByUserId(String userId);
}
