package com.guohuai.mmp.schedule.cyclesplit;

import com.google.common.collect.Lists;
import com.guohuai.cardvo.util.CardVoUtil;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 *
 *日切em持仓dao
 * @author yujianlong
 * @date 2018/4/1 18:39
 * @param
 * @return
 */
@Repository
public class DayCutPublisherHoldEMDao extends AbstractEMBatchDao<PublisherHoldEntity> {

	/**
	 *
	 *查询符合条件的预约持仓oid列表
	 * @author yujianlong
	 * @date 2018/4/1 18:38
	 * @param []
	 * @return java.util.List<java.lang.String>
	 */
	public List<String> getKDBHoldOids(String productOid){

		String querySql=
						"select hold.oid from t_money_publisher_hold hold " +
							" where hold.holdStatus = 'holding' " +
							" and hold.redeemableHoldVolume > 0 " +
							" and hold.productOid = :PRODUCTOID " ;
		Query query = em.createNativeQuery(querySql);
		query.setParameter("PRODUCTOID", productOid);
		return CardVoUtil.query2List(query);
	}

	/**
	 *查询符合条件的预约持仓oid列表并按照200人分组
	 *
	 * @author yujianlong
	 * @date 2018/4/19 15:59
	 * @param [productOid, defaultConfirmDate, splitNum]
	 * @return java.util.List<java.util.List<java.lang.String>>
	 */
	public List<List<String>> getKDBHoldOids(String productOid,String defaultConfirmDate,Integer splitNum){

		String querySql=
				"select ifnull(DATE_FORMAT(hold.confirmDate,'%Y-%m-%d'),'"+defaultConfirmDate+"') as confirmDate,hold.oid from t_money_publisher_hold hold " +
						" where hold.holdStatus = 'holding' " +
						" and hold.redeemableHoldVolume > 0 " +
						" and hold.productOid = :PRODUCTOID " ;
		Query query = em.createNativeQuery(querySql);
		query.setParameter("PRODUCTOID", productOid);

//		Comparator<Map<String, Object>> com=(m_1, m_2) -> {
//			String confirmDate1 = Objects.toString(m_1.get("confirmDate"));
//			String confirmDate2 =Objects.toString(m_2.get("confirmDate"));
//			return confirmDate1.compareTo(confirmDate2);
//		};
		return CardVoUtil.query2Map(query)
				.parallelStream()
//				.sorted(com)
				.collect(Collectors.groupingBy(m -> m.get("confirmDate"), Collectors.mapping(m -> Objects.toString(m.get("oid")), Collectors.toList())))
				.values().parallelStream().map(mm -> Lists.partition(mm, splitNum))
				.flatMap(t -> t.stream()).collect(Collectors.toList());
	}

	/**
	 *
	 *查询快定宝预约持仓总数
	 * @author yujianlong
	 * @date 2018/4/1 18:38
	 * @param []
	 * @return long
	 */
	public long getKDBHoldCount(String productOid) {
		String sql = " select count(1) from t_money_publisher_hold hold where hold.holdStatus = 'holding' and hold.redeemableHoldVolume > 0 and hold.productOid = :PRODUCTOID  ";
		Query query = em.createNativeQuery(sql);
		query.setParameter("PRODUCTOID", productOid);
		return CardVoUtil.countNum(query).longValue();
	}
}
