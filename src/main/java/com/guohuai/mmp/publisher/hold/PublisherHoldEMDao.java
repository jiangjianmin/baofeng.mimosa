package com.guohuai.mmp.publisher.hold;

import com.guohuai.cardvo.util.CardVoUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorOpenCycleRelationEntity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Desc: 持仓查询
 * @author huyong
 * @date 2018/3/21 下午4:15
 */
@Repository
public class PublisherHoldEMDao {

	/**
	 * 绑定jpa EntityManager
	 */
	@PersistenceContext
	private EntityManager em;

	/**
	 * @Desc: 快定宝订单总数
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	public Long getKDBHoldCount(MyKdbClientReq req,String userOid) {
		String sql = " SELECT "
				+ " COUNT(1) "
				+ " FROM t_money_investor_tradeorder mit "
				+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
				+ " WHERE  1 = 1 ";
		Query emQuery = kdbHoldSqlQuery(sql, req,userOid);
		return CardVoUtil.countNum(emQuery).longValue();
	}

	/**
	 * @Desc: 快定宝订单列表
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	public List<Map<String, Object>> getKDBHoldList(MyKdbClientReq req,String userOid) {
		String sql = " SELECT "
				+ " gp.name productName,mit.orderCode orderOid,'07' as subType,if(mit.orderType = 'bfPlusRedeem',pr.baseAmount,mit.orderAmount) as orderAmount, mit.orderType, "
				+ " CONCAT(TRUNCATE(gp.expAror*100, 2), '%') ratio,mit.expectIncome,TRUNCATE(mit.totalIncome,2) realIncome,"
				+ " DATE_FORMAT(mit.orderTime,'%Y-%m-%d %H:%i:%s') investTime,if(gp.type = 'PRODUCTTYPE_03',DATE_FORMAT(mit.beginAccuralDate,'%Y-%m-%d'),DATE_FORMAT(gp.setupDate,'%Y-%m-%d')) as setupDate,if(gp.type = 'PRODUCTTYPE_03',DATE_FORMAT(ADDDATE(mit.beginAccuralDate, gp.durationPeriodDays -1),'%Y-%m-%d'),DATE_FORMAT(gp.durationPeriodEndDate,'%Y-%m-%d')) as durationPeriodEndDate,mit.orderStatus,"
				+ " case when mit.orderStatus = 'accepted' and gp.type ='PRODUCTTYPE_03' and mit.holdStatus = 'toConfirm' then '申请中' "
				+ "      when mit.orderStatus = 'confirmed' and gp.type ='PRODUCTTYPE_03' and mit.holdStatus = 'holding' then '已受理' "
				+ "      when mit.orderType ='bfPlusRedeem' then '已转出' "
				+ "      when mit.orderStatus = 'invalidate' and (SELECT t3.assignment FROM t_money_investor_opencycle_tradeorder_relation t3 WHERE t3.sourceOrderCode = mit.orderCode) = 5 then '已转出' "
				+ " 	 else '' end  AS orderStatusTag,"
				+ " case when mit.orderType in('bfPlusRedeem','cash') then (select DATE_FORMAT(t1.orderTime,'%Y-%m-%d %H:%i:%s') investDate from t_money_investor_tradeorder t1,T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION t2 where t1.orderCode = t2.sourceOrderCode and t2.redeemOrderCode = mit.orderCode) "
				+ " 	 else '' end  AS originalOrderTime"
				+ " FROM t_money_investor_tradeorder mit "
				+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
				+ " LEFT JOIN t_money_investor_plus_redeem pr ON pr.oid = mit.oid"
				+ " WHERE  1 = 1 ";
		Query emQuery = kdbHoldSqlQuery(sql, req,userOid);
		List<Map<String, Object>> results = CardVoUtil.query2Map(emQuery);

		Collection<Object> ordercodetree = req.getOrdercodetree();
		//排序已完成
		if (null!=ordercodetree&&!ordercodetree.isEmpty()&&req.getHoldStatus() == 2){
			AtomicInteger ami=new AtomicInteger(1);
			Map<Object, Integer> baseCom = new HashMap<>();
			ordercodetree.stream().forEach(o->{
				baseCom.put(o,ami.getAndIncrement());
			});

			results = results.parallelStream().filter(m0 -> baseCom.containsKey(m0.get("orderOid"))).sorted((m1, m2) -> {
				Object code1 = m1.get("orderOid");
				Object code2 =m2.get("orderOid");
				return Integer.compare(baseCom.get(code1),baseCom.get(code2));
			}).collect(Collectors.toList());
		}
		return results;
	}


	/**
	 *递归累加存放排序ordercode
	 *
	 * @author yujianlong
	 * @date 2018/4/14 16:00
	 * @param [pCode, allCodeMap, investOrderCodeGroupMap, aSet]
	 * @return void
	 */
	private void recursiveAdd(Object pCode, Map<String, Map<String, Object>> allCodeMap, Map<Object, List<Object>> investOrderCodeGroupMap, LinkedHashSet<Object> aSet){
		List<Object> sCodes = investOrderCodeGroupMap.get(pCode);
		if (null!=sCodes&&!sCodes.isEmpty()){
			sCodes.stream().forEach(code->{
				Object redeemOrderCode=allCodeMap.get(code).get("redeemOrderCode");
				aSet.add(code);
				if(null!=redeemOrderCode){
					aSet.add(redeemOrderCode);
				};
			});
			sCodes.stream().forEach(code->{
				recursiveAdd(code,allCodeMap,investOrderCodeGroupMap,aSet);
			});
		}
	}

	/**
	 *
	 *获取排序树
	 * @author yujianlong
	 * @date 2018/4/13 17:59
	 * @param [ userOid]
	 * @return java.util.List<java.lang.String>
	 */
	public Collection<Object> getRelationTree(String userOid) {

		String sql =" select a.sourceOrderCode,a.investOrderCode,a.redeemOrderCode,a.orderType,a.assignment from t_money_investor_opencycle_tradeorder_relation a "
		+" where a.investorOid=:INVESTOROID "
		+" and a.orderType!='booking' "
		+" ORDER BY a.updateTime desc ";
		Query emQuery = em.createNativeQuery(sql);
		emQuery.setParameter("INVESTOROID",userOid);

		List<Map<String, Object>> maps = CardVoUtil.query2Map(emQuery);

		Map<String, Map<String, Object>> allCodeMap = maps.stream().collect(Collectors.toMap(
				t -> Objects.toString(t.get("sourceOrderCode"), ""),
				Function.identity(),
				(k, l) -> k,
				LinkedHashMap::new
		));
		Map<Object, List<Object>> investOrderCodeGroupMap = maps.stream().filter(o ->!Objects.isNull(o.get("investOrderCode"))).collect(Collectors.groupingBy(m -> m.get("investOrderCode"), Collectors.mapping(m -> m.get("sourceOrderCode"), Collectors.toList())));
		LinkedHashSet<Object> alist = new LinkedHashSet<>();

		allCodeMap.values().stream().filter(m->Objects.isNull(m.get("investOrderCode")))
				.forEach(m->{
					String sourceOrderCode=Objects.toString(m.get("sourceOrderCode"),"");
					String redeemOrderCode=Objects.toString(m.get("redeemOrderCode"),"");
					Integer assignment=Integer.valueOf(Objects.toString(m.get("assignment"),"0"));
					if (InvestorOpenCycleRelationEntity.ASSIGNMENTTYPE_ALLREDEEM!=assignment){
						alist.add(sourceOrderCode);
					}
					if (StringUtils.isNotEmpty(redeemOrderCode)){
						alist.add(redeemOrderCode);
					}
					recursiveAdd(sourceOrderCode,allCodeMap,investOrderCodeGroupMap,alist);
				});
		return alist;

	}



	/**
	 * @Desc: 快定宝订单查询条件
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	private Query kdbHoldSqlQuery(String selectSql, MyKdbClientReq req,String userOid) {
		if (StringUtils.isBlank(selectSql)) {
			return null;
		}
		boolean isCountSql = selectSql.toLowerCase().contains("count(");
		StringBuffer sb = new StringBuffer();
		Map<String, Object> paras = new HashMap<>();
		if (StringUtils.isNotBlank(userOid)) {
			sb.append(" AND mit.investorOid = :investorOid");
			paras.put("investorOid", userOid);
		}
		if (req.getHoldStatus() == 1) {
			sb.append(" AND mit.holdStatus IN ('toConfirm','holding') and gp.type in('PRODUCTTYPE_03','PRODUCTTYPE_04') ");
		} else if (req.getHoldStatus() == 2){
			sb.append(" AND (mit.holdStatus = 'closed' OR mit.orderType = 'bfPlusRedeem') AND gp.type = 'PRODUCTTYPE_04' and mit.orderStatus not in ('refunded', 'refused') ");
		}
		if (StringUtils.isNotBlank(req.getKdbStartDate())) {
			sb.append(" AND DATE_FORMAT(mit.orderTime,'%Y-%m-%d') >= :startDate");
			paras.put("startDate", req.getKdbStartDate());
		}
		if (StringUtils.isNotBlank(req.getKdbEndDate())) {
			sb.append(" AND DATE_FORMAT(mit.orderTime,'%Y-%m-%d') <= :endDate");
			paras.put("endDate", req.getKdbEndDate());
		}
		if (req.getHoldStatus() == 1) {
			//持有中排序
            sb.append(" ORDER BY mit.createTime desc ");

		} else if (req.getHoldStatus() == 2){//已完成排序
			sb.append(" AND mit.orderCode in (:ordercodetree) ");
			paras.put("ordercodetree", req.getOrdercodetree());
		}

        if (!isCountSql && req.getPage() > 0 && req.getRow() > 0) {
			sb.append(" limit ").append((req.getPage() - 1) * req.getRow()).append(",")
					.append(req.getRow());
		}
		Query emQuery = em.createNativeQuery(selectSql + sb);
		paras.forEach(emQuery::setParameter);
		return emQuery;
	}

	/**
	 * @Desc: 快定宝订单详情
	 * @author huyong
	 * @date 2018/3/22 下午4:15
	 */
	public Map<String, Object> getKDBHoldDetail(String orderCode,String userOid) {
		Map<String, Object> paras = new HashMap<>();
		String sql = " SELECT " +
				"  gp.name                                      productName, " +
				"  mit.oid                                      orderOid, " +
				"  mit.orderCode, " +
				"  gp.type                                   AS subType, " +
				"  mit.orderAmount, " +
				"  gp.oid                                       productOid, " +
				"  CONCAT(TRUNCATE(gp.expAror * 100, 2), '%')   ratio, " +
				"  mit.expectIncome, " +
				"  TRUNCATE(mit.totalIncome, 2)                 realIncome, " +
				"  DATE_FORMAT(mit.orderTime, '%Y-%m-%d')       investDate, " +
				"  DATE_FORMAT(mit.updateTime, " +
				"              '%Y-%m-%d')                      invalidDate, " +
				"  if(gp.type = 'PRODUCTTYPE_03', DATE_FORMAT(mit.beginAccuralDate, '%Y-%m-%d'), " +
				"     DATE_FORMAT(gp.setupDate, '%Y-%m-%d')) AS setupDate, " +
				"  if(gp.type = 'PRODUCTTYPE_03', DATE_FORMAT(ADDDATE(mit.beginAccuralDate, gp.durationPeriodDays - 1), '%Y-%m-%d'), " +
				"     DATE_FORMAT(gp.durationPeriodEndDate, " +
				"                 '%Y-%m-%d'))               AS durationPeriodEndDate, " +
				"  if(gp.type = 'PRODUCTTYPE_03', " +
				"     DATE_FORMAT(ADDDATE(mit.beginAccuralDate, gp.durationPeriodDays + gp.accrualRepayDays - 1), '%Y-%m-%d'), " +
				"     DATE_FORMAT(gp.repayDate, '%Y-%m-%d')) AS repayDate, " +
				"  DATE_FORMAT(mit.updateTime, " +
				"              '%Y-%m-%d')                      transferDate, " +
				"  gp.durationPeriodDays, " +
				"  gp.state                                     productStatus, " +
				"  mit.orderStatus, " +
				"  mit.orderType, " +
				"  mit.holdStatus, " +
				"  gp.instruction " +
				" FROM t_money_investor_tradeorder mit " +
				"  LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid " +
				" WHERE gp.type IN ('PRODUCTTYPE_03', 'PRODUCTTYPE_04') " +
				"      AND mit.orderCode = :orderCode " +
				"      AND mit.investorOid = :investorOid";
		paras.put("orderCode", orderCode);
		paras.put("investorOid", userOid);
		Query emQuery = em.createNativeQuery(sql);
		paras.forEach(emQuery::setParameter);
		List<Map<String, Object>> maps = CardVoUtil.query2Map(emQuery);
		if (CollectionUtils.isEmpty(maps)){
			return new HashMap<>();
		}
		return maps.get(0);
	}

	/**
	 * @Desc: 快定宝原投资订单
	 * @author huyong
	 * @date 2018/3/22下午4:15
	 */
	public List<Map<String, Object>> getKDBOriginalOrder(String orderCode,String userOid) {
		Map<String, Object> paras = new HashMap<>();
		String sql = "SELECT t1.sourceOrderCode,t1.sourceOrderAmount,DATE_FORMAT(t1.createTime,'%Y-%m-%d %H:%i:%s') orderTime"
				+ " FROM T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION t1 "
				+ " WHERE t1.investOrderCode = :orderCode"
				+ " AND t1.investorOid = :investorOid";
		paras.put("orderCode", orderCode);
		paras.put("investorOid", userOid);
		Query emQuery = em.createNativeQuery(sql);
		paras.forEach(emQuery::setParameter);
		return CardVoUtil.query2Map(emQuery);
	}

	/**
	 * @Desc: 快定宝未转出和已转出订单查询
	 * @author huyong
	 * @date 2018/3/22下午4:15
	 */
	public List<Map<String, Object>> getOrderTransf(List<String> listOrderCode,String userOid) {
		if (listOrderCode.size() <= 0) {
			return null;
		}
		Map<String, Object> paras = new HashMap<>();
		String sql = " SELECT "
				+ " gp.name productName,mit.orderCode orderCode,if(mit.orderType = 'bfPlusRedeem',mipr.baseAmount,mit.orderAmount) as orderAmount,mit.orderType,"
				+ " CONCAT(TRUNCATE(gp.expAror*100, 2), '%') ratio,if(mit.orderType = 'bfPlusRedeem',TRUNCATE(mit.totalIncome,2),mit.expectIncome) as realIncome,"
				+ " DATE_FORMAT(mit.orderTime,'%Y-%m-%d') transfDate,"
				+ " DATE_FORMAT(mit.updateTime,'%Y-%m-%d') invalidDate, DATE_FORMAT(gp.setupDate,'%Y-%m-%d') as setupDate,"
				+ " gp.durationPeriodDays,mit.orderStatus,gp.instruction,TRUNCATE(mipr.fee,2) poundage,if(mit.orderType = 'bfPlusRedeem',mit.orderAmount,mipr.baseAmount) as transfAmount"
				+ " FROM t_money_investor_tradeorder mit "
				+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
				+ " LEFT JOIN t_money_investor_plus_redeem mipr ON mipr.oid = mit.oid "
				+ " WHERE gp.type = 'PRODUCTTYPE_04'"
				+ " AND mit.orderCode in (:listOrderCode)"
				+ " AND mit.investorOid = :investorOid";
		paras.put("listOrderCode", listOrderCode);
		paras.put("investorOid", userOid);
		Query emQuery = em.createNativeQuery(sql);
		paras.forEach(emQuery::setParameter);
		return CardVoUtil.query2Map(emQuery);
	}

	/**
	 * @Desc: 查询全部提现或部分提现的订单
	 * @author huyong
	 * @date 2018/3/22下午4:15
	 */
//	public Map<String, Object> getRedeemOrder(String orderCode,String userOid) {
//		Map<String, Object> paras = new HashMap<>();
//		String sql = " SELECT "
//				+ " DATE_FORMAT(mipr.createDate,'%Y-%m-%d') transfDate,"
//				+ " TRUNCATE(mipr.fee,2) poundage,mipr.baseAmount transfAmount"
//				+ " FROM t_money_investor_plus_redeem mipr,t_money_investor_tradeorder mit"
//				+ " WHERE mipr.oid = mit.oid"
//				+ " and mit.orderCode = :orderCode"
//				+ " and mit.investorOid = :userOid";
//		paras.put("orderCode", orderCode);
//		paras.put("userOid", userOid);
//		Query emQuery = em.createNativeQuery(sql);
//		paras.forEach(emQuery::setParameter);
//		List<Map<String, Object>> maps = CardVoUtil.query2Map(emQuery);
//		if (CollectionUtils.isEmpty(maps)){
//			return new HashMap<>();
//		}
//		return maps.get(0);
//	}

	/**
	 * @Desc: 查询全部提现或部分提现的原订单
	 * @author huyong
	 * @date 2018/3/22下午4:15
	 */
	public Map<String, Object> getRedeemOriginalOrder(String orderCode,String userOid) {
		Map<String, Object> paras = new HashMap<>();
		String sql = " SELECT "
				+ " mit.orderAmount as transfAmount,mit.productOid,"
				+ " CONCAT(TRUNCATE(gp.expAror*100, 2), '%') as ratio,TRUNCATE(mit.totalIncome,2) as realIncome,"
				+ " DATE_FORMAT(rel.createTime,'%Y-%m-%d') investDate,DATE_FORMAT(gp.setupDate,'%Y-%m-%d') as setupDate,"
				+ " gp.durationPeriodDays,mit.orderStatus,gp.instruction,DATE_FORMAT(mipr.createDate,'%Y-%m-%d') as transfDate,"
				+ " TRUNCATE(mipr.fee,2) as poundage,mipr.baseAmount as orderAmount, gp.name as productName"
				+ " FROM t_money_investor_tradeorder mit "
				+ " LEFT JOIN t_gam_product gp ON mit.productOid = gp.oid "
				+ " LEFT JOIN t_money_investor_plus_redeem mipr ON mipr.oid = mit.oid "
				+ " LEFT JOIN T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION rel ON rel.redeemOrderCode = mit.orderCode "
				+ " WHERE gp.type = 'PRODUCTTYPE_04'"
				+ " AND mit.orderStatus in ('confirmed','done')"
				+ " AND mit.orderCode = :orderCode"
				+ " AND mit.investorOid = :investorOid";
		paras.put("orderCode", orderCode);
		paras.put("investorOid", userOid);
		Query emQuery = em.createNativeQuery(sql);
		paras.forEach(emQuery::setParameter);
		List<Map<String, Object>> maps = CardVoUtil.query2Map(emQuery);
		if (CollectionUtils.isEmpty(maps)){
			return new HashMap<>();
		}
		return maps.get(0);
	}
}
