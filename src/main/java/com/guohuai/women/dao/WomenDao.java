package com.guohuai.women.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;


public interface WomenDao extends JpaRepository<InvestorTradeOrderEntity, String>, JpaSpecificationExecutor<InvestorTradeOrderEntity>{

//	@Query(value="SELECT count(1) FROM t_money_investor_baseaccount a INNER JOIN  t_money_investor_tradeorder b ON a.oid = b.investorOid WHERE a.phoneNum=?1 AND b.payStatus='paySuccess' AND b.orderType='invest' AND b.orderStatus='paySuccess'",nativeQuery=true)
	@Query(value="SELECT COUNT(1) FROM t_money_investor_baseaccount a INNER JOIN  t_money_investor_tradeorder b ON a.oid = b.investorOid WHERE a.phoneNum=?1 AND b.orderType='invest' AND (b.orderStatus='paySuccess' OR b.orderStatus='accepted' OR b.orderStatus='confirmed' OR b.orderStatus='done')",nativeQuery=true)
	Long checkInvest(String phone);
//	@Query(value="SELECT COUNT(1) FROM t_money_investor_baseaccount a INNER JOIN  t_money_investor_tradeorder b ON a.oid = b.investorOid WHERE a.phoneNum=?1 AND b.orderType='invest' AND (b.orderStatus='paySuccess' OR b.orderStatus='accepted' OR b.orderStatus='confirmed' OR b.orderStatus='done') and b.createTime>=?2  and b.createTime<=?3",nativeQuery=true)
//	Long checkInvest(String phone,String startTime,String endTime);

}
