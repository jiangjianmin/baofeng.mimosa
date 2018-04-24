package com.guohuai.mmp.investor.tradeorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface FirstInvestDao extends JpaRepository<InvestorTradeOrderEntity, String>, JpaSpecificationExecutor<InvestorTradeOrderEntity>{
	@Query(value="SELECT COUNT(1) FROM t_money_investor_baseaccount a INNER JOIN  t_money_investor_tradeorder b ON a.oid = b.investorOid WHERE a.userOid=?1 AND b.orderType='invest' AND (b.orderStatus='paySuccess' OR b.orderStatus='accepted' OR b.orderStatus='confirmed' OR b.orderStatus='done')",nativeQuery=true)
	Long investTimes(String userId);
	@Query(value="SELECT COUNT(1) FROM t_money_investor_baseaccount a INNER JOIN  t_money_investor_tradeorder b ON a.oid = b.investorOid WHERE a.userOid=?1 AND b.orderType='invest' AND b.orderCode <= ?2 AND (b.orderStatus='paySuccess' OR b.orderStatus='accepted' OR b.orderStatus='confirmed' OR b.orderStatus='done')",nativeQuery=true)
	Long investTimesByOrderCode(String userId, String orderCode);
}
