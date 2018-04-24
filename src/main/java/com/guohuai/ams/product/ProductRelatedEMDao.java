package com.guohuai.ams.product;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.cardvo.util.CardVoUtil;
import com.guohuai.component.util.StringUtil;


/**
 * em的查询产品相关
 * @author yujianlong
 *
 */
@Repository
public class ProductRelatedEMDao {
	
	/**
	 * 绑定jpa EntityManager
	 */
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private ProductDao productDao;
	
	/**
	 * 获取所有定期产品
	 * @param ProductRelatedReq
	 * @return
	 */
	public List<Product> findAllProducts01(ProductRelatedReq productRelatedReq){
//		int page=productRelatedReq.getPage();
//		int rows=productRelatedReq.getPage();
//		String order=productRelatedReq.getOrder();
		String type=productRelatedReq.getType();
		String status=productRelatedReq.getStatus();
		String name=productRelatedReq.getName();
		String interestAuditStatus=productRelatedReq.getInterestAuditStatus();
		String raiseTimeBegin=productRelatedReq.getRaiseTimeBegin();
		String raiseTimeEnd=productRelatedReq.getRaiseTimeEnd();
//		String sort=productRelatedReq.getSort();
//		String isDeleted=productRelatedReq.getIsDeleted();
//		String auditState=productRelatedReq.getAuditState();
		
		Specification<Product> spec = new Specification<Product>() {
			@Override
			public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("isDeleted").as(String.class), Product.NO), cb.equal(root.get("auditState").as(String.class), Product.AUDIT_STATE_Reviewed));
			}
		};
		spec = Specifications.where(spec);

		Specification<Product> nameSpec = null;
		if (!StringUtil.isEmpty(name)) {
			nameSpec = new Specification<Product>() {
				@Override
				public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.or(cb.like(root.get("name").as(String.class), "%" + name + "%"), cb.like(root.get("fullName").as(String.class), "%" + name + "%"));
				}
			};
			spec = Specifications.where(spec).and(nameSpec);
		}
		Specification<Product> typeSpec = null;
		if (!StringUtil.isEmpty(type)) {
			typeSpec = new Specification<Product>() {
				@Override
				public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("type").get("oid").as(String.class), "PRODUCTTYPE_01");
				}
			};
			spec = Specifications.where(spec).and(typeSpec);
		}
		Specification<Product> statusSpec = null;
		if (!StringUtil.isEmpty(status)) {
			statusSpec = new Specification<Product>() {
				@Override
				public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("state").as(String.class), status);
				}
			};
			spec = Specifications.where(spec).and(statusSpec);
		}
		Specification<Product> interestAuditStatusSpec = null;
		if (!StringUtil.isEmpty(interestAuditStatus)) {
			interestAuditStatusSpec = new Specification<Product>() {
				@Override
				public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("interestAuditStatus").as(String.class), interestAuditStatus);
				}
			};
			spec = Specifications.where(spec).and(interestAuditStatusSpec);
		}
		Specification<Product> raiseTimeBeginSpec = null;
		if (!StringUtil.isEmpty(raiseTimeBegin)) {
			raiseTimeBeginSpec = new Specification<Product>() {
				@Override
				public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.greaterThanOrEqualTo(root.get("repayDate").as(String.class), raiseTimeBegin);
				}
			};
			spec = Specifications.where(spec).and(raiseTimeBeginSpec);
		}
		Specification<Product> raiseTimeEndSpec = null;
		if (!StringUtil.isEmpty(raiseTimeBegin)) {
			raiseTimeEndSpec = new Specification<Product>() {
				@Override
				public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.lessThanOrEqualTo(root.get("repayDate").as(String.class), raiseTimeEnd);
				}
			};
			spec = Specifications.where(spec).and(raiseTimeEndSpec);
		}
		return productDao.findAll(spec);
	}
	
	private Query searchCondition(String selectSql,ProductRelatedReq req){
		Query emQuery = null;
		if (StringUtils.isBlank(selectSql)) {
			return emQuery;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(selectSql);
		Map<String, Object> paras = new HashMap<>();
		if (StringUtils.isNotBlank(req.getIsDeleted())) {
			sb.append(" and g.isDeleted =:isDeleted ");
			paras.put("isDeleted", req.getIsDeleted());
		}
		if (StringUtils.isNotBlank(req.getAuditState())) {
			sb.append(" and g.auditState =:auditState ");
			paras.put("auditState", req.getAuditState());
		}
		if (StringUtils.isNotBlank(req.getName())) {
			sb.append(" and (g.name like :name or g.fullName like :name) ");
			paras.put("name", "%"+req.getName()+"%");
		}
		if (StringUtils.isNotBlank(req.getType())) {
			sb.append(" and g.type =:type ");
			paras.put("type", req.getType());
		}
		
		if (StringUtils.isNotBlank(req.getStatus())) {
			sb.append(" and g.state =:status ");
			paras.put("status", req.getStatus());
		}
		
		if (StringUtils.isNotBlank(req.getInterestAuditStatus())) {
			sb.append(" and g.interestAuditStatus =:interestAuditStatus ");
			paras.put("interestAuditStatus", req.getInterestAuditStatus());
		}
		
		if (StringUtils.isNotBlank(req.getRaiseTimeBegin())) {
			sb.append(" and g.repayDate >=:repayDatebegin ");
			paras.put("repayDatebegin", req.getRaiseTimeBegin());
		}
		
		if (StringUtils.isNotBlank(req.getRaiseTimeEnd())) {
			sb.append(" and g.repayDate <=:repayDateend ");
			paras.put("repayDateend", req.getRaiseTimeEnd());
		}
		
		emQuery = em.createNativeQuery(sb.toString());
		paras.forEach(emQuery::setParameter);
		return emQuery;
	}
	private Query searchConditionOnState(String selectSql,ProductRelatedReq req,boolean isGroupBy){
		Query emQuery = null;
		if (StringUtils.isBlank(selectSql)) {
			return emQuery;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(selectSql);
		Map<String, Object> paras = new HashMap<>();
		if (StringUtils.isNotBlank(req.getIsDeleted())) {
			sb.append(" and g.isDeleted =:isDeleted ");
			paras.put("isDeleted", req.getIsDeleted());
		}
		if (StringUtils.isNotBlank(req.getAuditState())) {
			sb.append(" and g.auditState =:auditState ");
			paras.put("auditState", req.getAuditState());
		}
		if (StringUtils.isNotBlank(req.getName())) {
			sb.append(" and (g.name like :name or g.fullName like :name) ");
			paras.put("name", "%"+req.getName()+"%");
		}
		if (StringUtils.isNotBlank(req.getType())) {
			sb.append(" and g.type =:type ");
			paras.put("type", req.getType());
		}
		if (StringUtils.isNotBlank(req.getStatus())) {
			sb.append(" and g.state =:status ");
			paras.put("status", req.getStatus());
		}
		sb.append(" and g.state in(:statusList) ");
		paras.put("statusList", Arrays.asList("DURATIONEND","CLEARING","CLEARED"));
		
		
		if (StringUtils.isNotBlank(req.getInterestAuditStatus())) {
			sb.append(" and g.interestAuditStatus =:interestAuditStatus ");
			paras.put("interestAuditStatus", req.getInterestAuditStatus());
		}
		
		if (StringUtils.isNotBlank(req.getRaiseTimeBegin())) {
			sb.append(" and g.repayDate >=:repayDatebegin ");
			paras.put("repayDatebegin", req.getRaiseTimeBegin());
		}
		
		if (StringUtils.isNotBlank(req.getRaiseTimeEnd())) {
			sb.append(" and g.repayDate <=:repayDateend ");
			paras.put("repayDateend", req.getRaiseTimeEnd());
		}
		if (isGroupBy) {
			sb.append(" group by productOid ");
		}
		
		emQuery = em.createNativeQuery(sb.toString());
		paras.forEach(emQuery::setParameter);
		return emQuery;
	}
	/**
	 * 获取产品信息
	 * @param productRelatedReq
	 * @return
	 */
	public List<Map<String,Object>> getProductInfo(ProductRelatedReq productRelatedReq){
		String selectSql=
				" select g.oid,g.type,g.raisedTotalNumber,a.cashPosition, "
				+" a.scale,a.cashtoolFactRate,g.recPeriodExpAnYield,g.state,IFNULL(g.expAror,0)*100 as expAror,g.collectedVolume, "
				+" g.incomeCalcBasis,g.durationPeriodDays from t_gam_product g "
				+" inner join t_gam_assetpool a "
				+" on a.oid=g.assetPoolOid ";
		
		return CardVoUtil.query2Map(searchCondition(selectSql, productRelatedReq));
		
	}
	public List<ProductRelatedInfoReq> getProductRelatedInfo(ProductRelatedReq productRelatedReq){
		List<Map<String,Object>> getProductInfo=getProductInfo(productRelatedReq);
		return getProductInfo.parallelStream().map(map->{
			return JSONObject.parseObject(JSONObject.toJSONString(map), ProductRelatedInfoReq.class);
		}).collect(Collectors.toList());
	}
	
	
	/**
	 * 根据产品ID查询存续期定期收益
	 * @param productRelatedReq
	 * @return
	 */
	public Map<String, BigDecimal> findSumIncomeAmount(ProductRelatedReq productRelatedReq){
		String selectSql="select a.productOid,sum(a.incomeAmount) as sumincomeAmount from T_MONEY_PRODUCT_RAISING_INCOME a "
				+"  inner join t_gam_product g "
				+" on g.oid=a.productOid "
				+" where 1=1 ";
		List<Map<String, Object>> resu=CardVoUtil.query2Map(searchConditionOnState(selectSql, productRelatedReq,true));
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("sumincomeAmount")).orElse("0").toString());
				}
				));
		
		return allMap;
	}
	public Map<String, BigDecimal> queryProductRaisingIncome(ProductRelatedReq productRelatedReq){
		String selectSql="select a.productOid,sum(incomeAmount) as sumincomeAmount from t_money_product_raising_income a "
				+"  inner join t_gam_product g "
				+" on g.oid=a.productOid "
				+" where 1=1 ";
		List<Map<String, Object>> resu=CardVoUtil.query2Map(searchConditionOnState(selectSql, productRelatedReq,true));
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("sumincomeAmount")).orElse("0").toString());
				}
				));
		
		return allMap;
	}
	public Map<String, BigDecimal> findSumTotalBaseIncome(ProductRelatedReq productRelatedReq){
		String selectSql="select a.productOid,sum(truncate(totalBaseIncome,2)) as sumtotalBaseIncome from T_MONEY_INVESTOR_TRADEORDER  a "
				+"  inner join t_gam_product g "
				+" on g.oid=a.productOid "
				+" where  a.orderType in ('invest','noPayInvest') and a.orderStatus = 'confirmed' ";
		List<Map<String, Object>> resu=CardVoUtil.query2Map(searchConditionOnState(selectSql, productRelatedReq,true));
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("sumtotalBaseIncome")).orElse("0").toString());
				}
				));
		
		return allMap;
	}
	
	public Map<String, BigDecimal> findSumTotalRewardIncome(ProductRelatedReq productRelatedReq){
		String selectSql="select a.productOid,sum(truncate(IFNULL(totalRewardIncome,0),2)+IFNULL(couponAmount,0)) as sumtotalRewardIncome from T_MONEY_INVESTOR_TRADEORDER  a "
				+"  inner join t_gam_product g "
				+" on g.oid=a.productOid "
				+" where  a.orderType in ('invest','noPayInvest') and a.orderStatus = 'confirmed' ";
		List<Map<String, Object>> resu=CardVoUtil.query2Map(searchConditionOnState(selectSql, productRelatedReq,true));
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("sumtotalRewardIncome")).orElse("0").toString());
				}
				));
		
		return allMap;
	}
	
	
	public Map<String, BigDecimal> getallocateInterestAuditRatio(ProductRelatedReq productRelatedReq){
		
		String selectSql=
				" select a.productOid,a.ratio from T_GAM_ALLOCATE_INTEREST_AUDIT a  "
				+"  inner join t_gam_product g "
				+" on g.oid=a.productOid "
				;
		List<Map<String, Object>> resu=CardVoUtil.query2Map(searchConditionOnState(selectSql, productRelatedReq,true));
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("ratio")).orElse("0").toString());
				}
				));
		
		return allMap;
		
	}
	
//	public Map<String, BigDecimal> findProductInterestToAudit(ProductRelatedReq productRelatedReq){
//		String selectSql="select * from T_GAM_ALLOCATE_INTEREST_AUDIT  a "
//				+"  inner join t_gam_product g "
//				+" on g.oid=a.productOid ";
//		
//		String re=Optional.ofNullable(CardVoUtil.query2List(searchConditionOnState(selectSql, productRelatedReq,true))).map(l->l.get(0)).orElse("0").toString();
//		return new BigDecimal(re);
//	}
	
	public List<Map<String,Object>> findInterestToAudit(ProductRelatedReq productRelatedReq){
		String selectSql="select g.expAror,g.collectedVolume,g.incomeCalcBasis,a.ratio from t_gam_product g "
				+" inner join T_GAM_ALLOCATE_INTEREST_AUDIT a  "
				+" on g.oid=a.productOid ";
		
		return CardVoUtil.query2Map(searchConditionOnState(selectSql, productRelatedReq,true));
		
	}
	
	public Map<String, String> getCorporateNamesByOids(Collection<String> corporateIds,Map<String, String> poid_corporateId){
		if (null==poid_corporateId||poid_corporateId.isEmpty()||null==corporateIds||corporateIds.isEmpty()) {
			return new HashMap<>();
		}
		
		String selectSql="select a.oid,a.name from T_MONEY_CORPORATE a where a.oid in (:OIDS)";
		Query emQuery = em.createNativeQuery(selectSql.toString());
		emQuery.setParameter("OIDS", corporateIds);
		List<Map<String, Object>> resu=CardVoUtil.query2Map(emQuery);
		Map<String, String> allMap=resu.parallelStream().collect(Collectors.toMap(m->(String)m.get("oid"), m->(String)m.get("name")));
		Map<String, String> allresu=
		poid_corporateId.entrySet().parallelStream().collect(Collectors.toMap(
				entry->entry.getKey(), 
				entry->{
					return Optional.ofNullable(allMap.get(entry.getValue())).orElse("");
				}
				));
		return allresu;
	}
	public Map<String, BigDecimal> getSumIncomeAmountByPoids(Collection<String> poids){
		if (null==poids||poids.isEmpty()) {
			return new HashMap<>();
		}
		String selectSql=
		" select a.productOid ,sum(a.incomeAmount) as sumincomeAmount from T_MONEY_PRODUCT_RAISING_INCOME a  "
		+" where a.productOid in (:poids) "
		+" group by a.productOid " ;
		Query emQuery = em.createNativeQuery(selectSql.toString());
		emQuery.setParameter("poids", poids);
		List<Map<String, Object>> resu=CardVoUtil.query2Map(emQuery);
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("sumincomeAmount")).orElse("0").toString());
				}
				));
		
		return allMap;
	}
	public Map<String, BigDecimal> getSumRewardIncomeByPoids(Collection<String> poids){
		if (null==poids||poids.isEmpty()) {
			return new HashMap<>();
		}
		String selectSql=
						"  select a.productOid,sum(truncate(IFNULL(a.totalRewardIncome,0),2)+IFNULL(a.couponAmount,0)) as sumtotalRewardIncome "
						+"  from T_MONEY_INVESTOR_TRADEORDER a "
						+"  where a.productOid in (:poids) and a.orderType in  "
						+"  ('invest','noPayInvest') and a.orderStatus = 'confirmed' "
						+"  GROUP BY a.productOid ";
		Query emQuery = em.createNativeQuery(selectSql.toString());
		emQuery.setParameter("poids", poids);
		List<Map<String, Object>> resu=CardVoUtil.query2Map(emQuery);
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("sumtotalRewardIncome")).orElse("0").toString());
				}
				));
		
		return allMap;
	}
	public Map<String, BigDecimal> getSumBaseIncomeByPoids(Collection<String> poids){
		if (null==poids||poids.isEmpty()) {
			return new HashMap<>();
		}
		String selectSql=
				"  select a.productOid,sum(truncate(a.totalBaseIncome,2))  as sumtotalBaseIncome"
						+"  from T_MONEY_INVESTOR_TRADEORDER a "
						+"  where a.productOid in (:poids) and a.orderType in  "
						+"  ('invest','noPayInvest') and a.orderStatus = 'confirmed' "
						+"  GROUP BY a.productOid ";
		Query emQuery = em.createNativeQuery(selectSql.toString());
		emQuery.setParameter("poids", poids);
		List<Map<String, Object>> resu=CardVoUtil.query2Map(emQuery);
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("sumtotalBaseIncome")).orElse("0").toString());
				}
				));
		
		return allMap;
	}
	public Map<String, BigDecimal> getallocateInterestAuditRatioByPoids(Collection<String> poids){
		if (null==poids||poids.isEmpty()) {
			return new HashMap<>();
		}
		String selectSql=
				" select productOid,ratio from T_GAM_ALLOCATE_INTEREST_AUDIT where productOid in (:poids) and auditStatus in('TOAUDIT','AUDITPASS') ";
					
		Query emQuery = em.createNativeQuery(selectSql.toString());
		emQuery.setParameter("poids", poids);
		List<Map<String, Object>> resu=CardVoUtil.query2Map(emQuery);
		Map<String, BigDecimal> allMap=resu.parallelStream().collect(Collectors.toMap(
				m->(String)m.get("productOid"),
				m->{
					return new BigDecimal(Optional.ofNullable(m.get("ratio")).orElse("0").toString());
				}
				));
		
		return allMap;
	}
	
	
	
}
