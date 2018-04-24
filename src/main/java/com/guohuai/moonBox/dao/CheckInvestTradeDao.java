package com.guohuai.moonBox.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;

@Repository
public interface CheckInvestTradeDao extends JpaRepository<InvestorTradeOrderEntity, String>, JpaSpecificationExecutor<InvestorTradeOrderEntity>{

	@Query(value="select count(1) from t_money_investor_tradeorder  where investorOid=?1 and ProductOid=?2 and isAuto=?3 and createTime between ?4 and ?5 ",nativeQuery=true)
	Long checkInvest(String investorOid,String productOid,String isAuto,String beginMonth,String endMonth);
 

}
