package com.guohuai.ams.companyScatterStandard;

import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.cardvo.util.CardVoUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.schedule.cyclesplit.AbstractEMBatchDao;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * em订单电子签章dao
 * @author yujianlong
 *
 */
@Repository
public class ElectronicSignatureRelationEmDao extends AbstractEMBatchDao<ElectronicSignatureRelation> {


	/**
	 *
	 *查询需要打电子签章的订单信息
	 * @author yujianlong
	 * @date 2018/4/23 14:09
	 * @param [productOid]
	 * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
	 */
	public List<Map<String, Object>> getTradeInfos(String productOid){
		String baseSql="select a.orderCode,b.idNum,b.realName,c.* from t_money_investor_tradeorder a  " +
				" left join t_money_investor_baseaccount b on b.userOid=a.investorOid " +
				" left join t_gam_tradeorder_electronicsignatrue_relation c on c.orderCode=a.orderCode "
				+ "where c.productOid = :PRODUCTOID and c.electronicSignatureUrl is null ";
		Query emQuery = em.createNativeQuery(baseSql);
		emQuery.setParameter("PRODUCTOID", productOid);
		return CardVoUtil.query2Map(emQuery);

	}

	/**
	 *获取需要手动处理募集满额的企业散标
	 *
	 * @author yujianlong
	 * @date 2018/4/23 16:38
	 * @param [productOids]
	 * @return java.util.List<com.guohuai.basic.message.DealMessageEntity>
	 */
	public List<DealMessageEntity> getScatter2DurationingProductInfos(List<String> productOids){
		String baseSql=" SELECT a.productOid,ifnull(DATE_FORMAT(MAX(a.orderTime),'%Y-%m-%d'),DATE_FORMAT(NOW(),'%Y-%m-%d')) as orderTime from t_money_investor_tradeorder a where a.productOid in(:PRODUCTOIDS) GROUP BY a.productOid ";
		Query emQuery = em.createNativeQuery(baseSql);
		emQuery.setParameter("PRODUCTOIDS", productOids);
		List<Map<String, Object>> maps = CardVoUtil.query2Map(emQuery);
		List<DealMessageEntity> dealMessageEntitys = maps.parallelStream().map(m -> {
			String productOid = Objects.toString(m.get("productOid"));
			String orderTime = Objects.toString(m.get("orderTime"));
			DealMessageEntity messageEntity = new DealMessageEntity();
			messageEntity.setOrderTime(DateUtil.parse(orderTime,"yyyy-MM-dd"));
			messageEntity.setTriggerProductOid(productOid);
			return messageEntity;
		}).collect(Collectors.toList());
		return dealMessageEntitys;

	}
	/**
	 * 根据订单号查询
	 */
	public ElectronicSignatureRelation findByOrderCode(String orderCode){
		String baseSql="select * from t_money_investor_tradeorder_electronicsignatrue_relation where orderCode=:orderCode";
		Query emQuery = em.createNativeQuery(baseSql);
		emQuery.setParameter("orderCode", orderCode);
		Object result=emQuery.getSingleResult();
		if(result == null) return null;
		return (ElectronicSignatureRelation) result;
	}
}
