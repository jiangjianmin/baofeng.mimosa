package com.guohuai.mmp.schedule.cyclesplit;

import com.guohuai.cardvo.util.CardVoUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 日切订单em dao
 * @author yujianlong
 *
 */
@Repository
public class DayCutTradeOrderEMDao extends AbstractEMBatchDao<InvestorTradeOrderEntity> {


	/**
	 *
	 *查询给定持仓oid对应的用户id和订单orderCode列表
	 * @author yujianlong
	 * @date 2018/4/1 18:41
	 * @param [holdOids]
	 * @return java.util.Map<java.lang.String,java.util.List<java.lang.String>>
	 */
	public Map<String, List<String>> getInvest_holds(List<String> holdOids){
		String baseSql="select a.orderCode,a.investorOid from t_money_investor_tradeorder a  "
				+ "where a.holdOid in(:HOLDOIDS)";
		javax.persistence.Query emQuery = em.createNativeQuery(baseSql);
		emQuery.setParameter("HOLDOIDS", holdOids);
		List<Map<String, Object>> result= CardVoUtil.query2Map(emQuery);
		return result.stream().collect(
				Collectors.groupingBy(m -> Objects.toString(m.get("investorOid")), Collectors.mapping(m ->Objects.toString(m.get("orderCode")), Collectors.toList()))
		);

	}




}
