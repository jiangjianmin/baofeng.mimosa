package com.guohuai.cardvo.dao;

import com.google.common.base.Objects;
import com.guohuai.ams.activityModel.ProductPlaceEntity;
import com.guohuai.basic.cardvo.req.userInfoReq.CardbalanceReq;
import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.basic.cardvo.req.userInfoReq.TimePageReq;
import com.guohuai.basic.cardvo.req.userInfoReq.UserInvedtorOidsReq;
import com.guohuai.cardvo.req.ProductReq;
import com.guohuai.cardvo.util.CardVoUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * 【新手包】查询dao 直接用原生jpa sql查询
 * 
 * @author yujianlong
 *
 */
@Repository
public class MimosaDao {

	/**
	 * 绑定jpa EntityManager
	 */
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 获取产品信息作为判断触发条件的依据
	 * yujianlong
	 * @param productOid
	 * @return
	 */
	public Map<String,Object> getProductInfoOnTrigger(String productOid) {
		String sql = " select b.oid,b.isActivityProduct,b.`name`,b.`code`,b.type,b.guessOid,t3.labelCode from t_gam_product b  "
				+" LEFT JOIN t_money_platform_label_product t2 ON b.oid = t2.productOid "
				+" LEFT JOIN t_money_platform_label t3 ON t2.labelOid = t3.oid "
				+" where b.oid=:PRODUCTOID ";
		
		Query emQuery = em.createNativeQuery(sql);
		emQuery.setParameter("PRODUCTOID", productOid);
		List<Map<String,Object>> productInfoOnTrigger=CardVoUtil.query2Map(emQuery);
		
		return productInfoOnTrigger.isEmpty()?null:productInfoOnTrigger.get(0);
	}
	
	/**
	 * 根据useroid获取用户手机号和姓名和绑卡信息
	 * @param userOid
	 * @return
	 */
	public Map<String,String> getUserInfoByUserId(String userOid) {
		Map<String,String> u2=new HashMap<>();
		if (StringUtils.isBlank(userOid)) {
			return u2;
		}
		String sql = "select a.realName,a.phoneNum,a.idNum  from t_money_investor_baseaccount a where a.userOid=:USEROID  ";
		Query emQuery = em.createNativeQuery(sql);
		emQuery.setParameter("USEROID", userOid);
		List<Map<String,Object>> infos=CardVoUtil.query2Map(emQuery);
		if (null==infos||infos.isEmpty()) {
			return u2;
		}
		Map<String,Object> u1=infos.get(0);
		if (null==u1||u1.isEmpty()) {
			return u2;
		}
		String idNUm=Optional.ofNullable(u1.get("idNum")).orElse("").toString();
		if (StringUtils.isBlank(idNUm)) {
			idNUm="no";
		}else{
			idNUm="yes";
		}
		u2.put("username", Optional.ofNullable(u1.get("realName")).orElse("").toString());
		u2.put("phone", Optional.ofNullable(u1.get("phoneNum")).orElse("").toString());
		u2.put("idNum", idNUm);
		return u2;
	}
	
	
	
	
	
	
	

	/**
	 * publisherHoldSql查询拼接
	 * @param selectSql
	 * @param mUAllReq
	 * @return
	 */
	private Query publisherHoldSqlQuery(String selectSql, MUAllReq mUAllReq) {

		if (StringUtils.isBlank(selectSql)) {
			return null;
		}
		boolean isCountSql = selectSql.toLowerCase().contains("count(");
		StringBuffer sb1 = new StringBuffer();
		Map<String, Object> paras = new HashMap<>();
		String joinField = "left";
		if (StringUtils.isNotBlank(mUAllReq.getProductType())) {
			joinField = "inner";
			sb1.append(" AND gp.type = :PRODUCTTYPE   ");
			paras.put("PRODUCTTYPE", mUAllReq.getProductType());
		}
		if (StringUtils.isNotBlank(mUAllReq.getProductName())) {
			joinField = "inner";
			sb1.append(" AND gp.name like :PRODUCTNAME   ");
			paras.put("PRODUCTNAME", mUAllReq.getProductName() + "%");
		}
		StringBuffer sb2 = new StringBuffer();
		if (null != mUAllReq.getTotalInvestAmountStart()) {
			boolean isZero=mUAllReq.getTotalInvestAmountStart().equals(BigDecimal.ZERO);
			if (isZero) {
				sb2.append(" and (tmits.totalInvestAmount>= :TOTALINVESTAMOUNTSTART or tmits.totalInvestAmount is null)");
			}else{
				sb2.append(" and tmits.totalInvestAmount>= :TOTALINVESTAMOUNTSTART");
			}
			paras.put("TOTALINVESTAMOUNTSTART", mUAllReq.getTotalInvestAmountStart());
		}
		if (null != mUAllReq.getTotalInvestAmountEnd()) {
			boolean isZero=mUAllReq.getTotalInvestAmountEnd().equals(BigDecimal.ZERO);
			if (isZero) {
				sb2.append(" and (tmits.totalInvestAmount<= :TOTALINVESTAMOUNTEND or tmits.totalInvestAmount is null)");
			}else{
				sb2.append(" and tmits.totalInvestAmount<= :TOTALINVESTAMOUNTEND   ");
			}
			
			paras.put("TOTALINVESTAMOUNTEND", mUAllReq.getTotalInvestAmountEnd());
		}
		if (null != mUAllReq.getCInvestAmountStart()) {
			boolean isZero=mUAllReq.getCInvestAmountStart().equals(BigDecimal.ZERO);
			if (isZero) {
				sb2.append(" and (investingAmout>= :CINVESTAMOUNTSTART or investingAmout is null)");
			}else{
				sb2.append(" and mphnew.investingAmout>=:CINVESTAMOUNTSTART");
			}
			
			paras.put("CINVESTAMOUNTSTART", mUAllReq.getCInvestAmountStart());
		}
		if (null != mUAllReq.getCInvestAmountEND()) {
			boolean isZero=mUAllReq.getCInvestAmountEND().equals(BigDecimal.ZERO);
			if (isZero) {
				sb2.append(" and (investingAmout<= :CINVESTAMOUNTEND or investingAmout is null)");
			}else{
				sb2.append(" and mphnew.investingAmout<=:CINVESTAMOUNTEND");
			}
			
			paras.put("CINVESTAMOUNTEND", mUAllReq.getCInvestAmountEND());
		}
		if (mUAllReq.isIdListNotEmpty()) {
			sb2.append(" and mib.userOid in (:USEROIDS) ");
			paras.put("USEROIDS", mUAllReq.getIdList());
		}
		if (null != mUAllReq.getInvested()) {
			if(Objects.equal(mUAllReq.getInvested(), 1)) {
				sb2.append(" and tmits.totalInvestAmount > 0 ");
			}else if(Objects.equal(mUAllReq.getInvested(), 2)) {
				sb2.append(" and tmits.totalInvestAmount is null ");
			}
		}
		if (null != mUAllReq.getLastTradeTimeStart()) {
			sb2.append("and tmits.lastTradeTime >= :LASTTRADETIMESTART ");
			paras.put("LASTTRADETIMESTART", mUAllReq.getLastTradeTimeStart());
		}
		if (null != mUAllReq.getLastTradeTimeEnd()) {
			sb2.append("and tmits.lastTradeTime <= :LASTTRADETIMEEND ");
			paras.put("LASTTRADETIMEEND", mUAllReq.getLastTradeTimeEnd());
		}
		if (null != mUAllReq.getLockStatus()) {
			sb2.append(" and mib.cardlockstatus = :CARDLOCKSTATUS ");
			paras.put("CARDLOCKSTATUS", mUAllReq.getLockStatus());
		}
		StringBuffer splitPage = new StringBuffer();
		if (!isCountSql && mUAllReq.isNeedPage()) {
			splitPage.append(" limit ").append((mUAllReq.getPage() - 1) * mUAllReq.getRows()).append(",")
					.append(mUAllReq.getRows());
		}
		String middle = " FROM t_money_investor_baseaccount mib " + joinField + " JOIN (  " + " SELECT   "
				+ " mph.investorOid investorOid,  " 
//				+ " SUM(mph.totalInvestVolume) totalInvestAmount,  "
				+ " SUM(mph.holdVolume) investingAmout  "
				+ " FROM t_money_publisher_hold mph LEFT JOIN t_gam_product gp ON mph.productOid = gp.oid"
				+ " WHERE gp.code != 'BFJRTYJLC'  " + sb1.toString()
				+ " GROUP BY mph.investorOid  " + " ) mphnew ON mib.oid = mphnew.investorOid "
				+ " LEFT JOIN t_money_investor_tradeorder_statistics tmits ON tmits.investorOid = mib.oid"
				+ " where 1=1 " + sb2.toString() + splitPage;

		Query emQuery = em.createNativeQuery(selectSql + middle);
		paras.forEach(emQuery::setParameter);
		return emQuery;
	}

	/**
	 * 获取数量publisherHoldSql
	 * @param mUAllReq
	 * @return
	 */
	public Long counPublisherHold(MUAllReq mUAllReq) {
		String selectedSql = "select COUNT(1)  ";

		Query emQuery = publisherHoldSqlQuery(selectedSql, mUAllReq);
		
		return CardVoUtil.countNum(emQuery).longValue();
	}

	/**
	 * publisherHoldSql获取map
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsPublisherHold(MUAllReq mUAllReq) {
		// sql语句最好分离 方便count和query
		String selectedSql = " SELECT  " + " mib.oid investorOid, "
				+ " IFNULL(tmits.totalInvestAmount,0) totalInvestAmount, "
				+ " IFNULL(mphnew.investingAmout,0) investingAmout ";

		Query emQuery = publisherHoldSqlQuery(selectedSql, mUAllReq);
		return CardVoUtil.query2Map(emQuery);

	}

	/**
	 * publisherHoldSql获取ids
	 * @param mUAllReq
	 * @return
	 */
	public List<String> query2IdListPublisherHold(MUAllReq mUAllReq) {
		// sql语句最好分离 方便count和query
		String selectedSql = " SELECT  " + " mib.oid investorOid ";
		Query emQuery = publisherHoldSqlQuery(selectedSql, mUAllReq);
		return CardVoUtil.query2List(emQuery);
	}

	

	/**
	 * 只是mimosaStastics不为空的查询条件
	 * 
	 * @param selectSql
	 * @param mUAllReq
	 * @return
	 */
	private Query sqlConditionOnlyInvestorStatistics(String selectSql, MUAllReq mUAllReq) {
		Query emQuery = null;
		// select语句为空情况
		if (StringUtils.isBlank(selectSql)) {
			return emQuery;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(selectSql);// 首先添加查询条件
		// 依次判断mUAllReq的所有 t_wfd_user相关属性
		// Invested
		if (null != mUAllReq.getInvested()) {

			if (mUAllReq.getInvested() == 1) {
				// 投资过
				sb.append("and tmits.totalInvestAmount > 0 ");
			}
			if (mUAllReq.getInvested() == 2) {
				// 未资过
				sb.append("and tmits.totalInvestAmount is null ");
			}
		}
		if (null != mUAllReq.getCInvestAmountStart()) {
			// 在投
			sb.append("and tmits.totalInvestAmount-a.totalRedeemAmount>= :CINVESTAMOUNTSTART ");
		}
		if (null != mUAllReq.getCInvestAmountEND()) {
			// 在投
			sb.append("and tmits.totalInvestAmount-a.totalRedeemAmount<= :CINVESTAMOUNTEND ");
		}
		if (null != mUAllReq.getTotalInvestAmountStart()) {
			// 累投
			sb.append("and tmits.totalInvestAmount>= :TOTALINVESTAMOUNTSTART ");
		}
		if (null != mUAllReq.getTotalInvestAmountEnd()) {
			// 累投
			sb.append("and tmits.totalInvestAmount<= :TOTALINVESTAMOUNTEND ");
		}

		if (null != mUAllReq.getLockStatus()) {
			sb.append(" and a.cardlockstatus= :CARDLOCKSTATUS ");
		}

		// idList
		if (mUAllReq.isIdListNotEmpty()) {
			sb.append(" and a.investorOid in (:USEROIDS) ");
		}
		if (StringUtils.isNotBlank(mUAllReq.getOrderField())) {
			String orderSequence = "desc";
			if (StringUtils.isNotBlank(mUAllReq.getOrderSequence())) {
				orderSequence = mUAllReq.getOrderSequence();
			}
			sb.append(" order by a.").append(mUAllReq.getOrderField() + " ").append(orderSequence + " ");
		}
		// 分页
		// 复合条件，而且当前库作为全量导入，则不使用分页
		if (!selectSql.toLowerCase().contains("count(") && mUAllReq.isNeedPage()) {
			sb.append(" limit ").append((mUAllReq.getPage() - 1) * mUAllReq.getRows()).append(",")
					.append(mUAllReq.getRows());
		}

		emQuery = em.createNativeQuery(sb.toString());

		if (null != mUAllReq.getCInvestAmountStart()) {
			// 在投
			emQuery.setParameter("CINVESTAMOUNTSTART", mUAllReq.getCInvestAmountStart());
		}
		if (null != mUAllReq.getCInvestAmountEND()) {
			// 在投
			emQuery.setParameter("CINVESTAMOUNTEND", mUAllReq.getCInvestAmountEND());
		}
		if (null != mUAllReq.getTotalInvestAmountStart()) {
			// 累投
			emQuery.setParameter("TOTALINVESTAMOUNTSTART", mUAllReq.getTotalInvestAmountStart());
		}
		if (null != mUAllReq.getTotalInvestAmountEnd()) {
			emQuery.setParameter("TOTALINVESTAMOUNTEND", mUAllReq.getTotalInvestAmountEnd());
			// 累投
		}
		if (null != mUAllReq.getLockStatus()) {
			emQuery.setParameter("CARDLOCKSTATUS", mUAllReq.getLockStatus());
		}

		// idList
		if (mUAllReq.isIdListNotEmpty()) {
			emQuery.setParameter("USEROIDS", mUAllReq.getIdList());
		}
		// =====
		return emQuery;
	}

	/**
	 * /** 只product查询条件，其他条件没有
	 * 
	 * @param selectSql
	 * @param mUAllReq
	 * @return
	 */
	private Query sqlConditionOnlyProduct(String selectSql, MUAllReq mUAllReq) {
		boolean isCountSql = selectSql.toLowerCase().contains("count(");
		// String tableName=isCountSql?"b":"a";
		// select count(*) from t_money_investor_statistics a right join
		// t_money_investor_tradeorder b
		// on b.investorOid =a.investorOid
		// where 1=1 and b.productOid ='226f412530ff4e41bc9b8ae54d36582f'
		// limit 10;
		Query emQuery = null;
		/*
		 * if (!mUAllReq.isFromMimosaProductOnly()) { return emQuery; }
		 */
		if (StringUtils.isBlank(selectSql)) {
			return emQuery;
		}

		StringBuffer sb = new StringBuffer();
		sb.append(selectSql);// 首先添加查询条件
		if (null != mUAllReq.getLockStatus()) {
			sb.append(" and a.cardlockstatus= :CARDLOCKSTATUS ");
		}
		if (StringUtils.isNotBlank(mUAllReq.getProductName())) {
			sb.append("  and a.productOid in (select oid from t_gam_product k where k.name like :PRODUCTNAME ) ");

		}
		if (StringUtils.isNotBlank(mUAllReq.getProductType())) {
			sb.append(" and a.productOid in (select oid from t_gam_product k where k.type = :PRODUCTTYPE) ");
		}
		// List<String> POIDS = null;
		// if (StringUtils.isNotBlank(mUAllReq.getProductName())) {
		// POIDS = queryProductOidByName(mUAllReq);
		// }
		// if (StringUtils.isNotBlank(mUAllReq.getProductType())) {
		//
		// List<String> POIDSByType = queryProductOidsByType(mUAllReq);
		// if (null != POIDS) {
		// POIDS.retainAll(POIDSByType);// 求交集
		// } else {
		// POIDS = POIDSByType;
		// }
		// }
		//
		// if (null != POIDS) {
		// // 查询type/name对应的所有oids
		// sb.append(" and a.productOid in (:POIDS) ");
		// }
		if (null != mUAllReq.getTradeTimeStart()) {
			sb.append("and a.orderTime>= :TRADETIMESTART ");
		}
		// createTimeEnd
		if (null != mUAllReq.getTradeTimeEnd()) {
			sb.append("and a.orderTime<= :TRADETIMEEND ");
		}
		if (null != mUAllReq.getLastTradeTimeStart()) {

			sb.append("and b.orderTime>= :LASTTRADETIMESTART ");
		}
		if (null != mUAllReq.getLastTradeTimeEnd()) {
			sb.append("and b.orderTime<= :LASTTRADETIMEEND ");
		}
		if (null != mUAllReq.getOrderTypeList()) {
			sb.append("and a.orderType in(:ORDERTYPELIST) ");

		}
		if (null != mUAllReq.getOrderStatusList()) {
			sb.append("and a.orderStatus in(:ORDERSTATUSLIST) ");
		}
		// idList
		if (mUAllReq.isIdListNotEmpty()) {
			sb.append(" and a.investorOid in (:USEROIDS) ");
		}

		if (StringUtils.isNotBlank(mUAllReq.getOrderField())) {
			String orderSequence = "desc";
			if (StringUtils.isNotBlank(mUAllReq.getOrderSequence())) {
				orderSequence = mUAllReq.getOrderSequence();
			}
			sb.append(" order by a.").append(mUAllReq.getOrderField() + " ").append(orderSequence + " ");
		}
		// 分页
		// 复合条件，而且当前库作为全量导入，则不使用分页
		if (!isCountSql && mUAllReq.isNeedPage()) {
			sb.append(" limit ").append((mUAllReq.getPage() - 1) * mUAllReq.getRows()).append(",")
					.append(mUAllReq.getRows());
		}

		emQuery = em.createNativeQuery(sb.toString());
		if (null != mUAllReq.getLockStatus()) {
			emQuery.setParameter("CARDLOCKSTATUS", mUAllReq.getLockStatus());
		}
		if (StringUtils.isNotBlank(mUAllReq.getProductType())) {
			emQuery.setParameter("PRODUCTTYPE", mUAllReq.getProductType());
		}
		if (StringUtils.isNotBlank(mUAllReq.getProductName())) {
			emQuery.setParameter("PRODUCTNAME", mUAllReq.getProductName() + "%");
		}
		// if (StringUtils.isNotBlank(mUAllReq.getProductName()) ||
		// StringUtils.isNotBlank(mUAllReq.getProductType())) {
		//
		// emQuery.setParameter("POIDS", POIDS);
		// }
		if (null != mUAllReq.getTradeTimeStart()) {
			emQuery.setParameter("TRADETIMESTART", mUAllReq.getTradeTimeStart());
		}
		// createTimeEnd
		if (null != mUAllReq.getTradeTimeEnd()) {
			emQuery.setParameter("TRADETIMEEND", mUAllReq.getTradeTimeEnd());
		}

		if (null != mUAllReq.getLastTradeTimeStart()) {
			emQuery.setParameter("LASTTRADETIMESTART", mUAllReq.getLastTradeTimeStart());
		}
		if (null != mUAllReq.getLastTradeTimeEnd()) {
			emQuery.setParameter("LASTTRADETIMEEND", mUAllReq.getLastTradeTimeEnd());
		}
		if (null != mUAllReq.getOrderTypeList()) {
			emQuery.setParameter("ORDERTYPELIST", mUAllReq.getOrderTypeList());

		}
		if (null != mUAllReq.getOrderStatusList()) {
			emQuery.setParameter("ORDERSTATUSLIST", mUAllReq.getOrderStatusList());
		}
		// idList
		if (mUAllReq.isIdListNotEmpty()) {
			emQuery.setParameter("USEROIDS", mUAllReq.getIdList());
		}
		return emQuery;
	}

	/**
	 * 带有product和mimosa条件的查询
	 * 
	 * @param selectSql
	 * @param mUAllReq
	 * @return
	 */
	private Query sqlConditionProduct(String selectSql, MUAllReq mUAllReq) {
		boolean isCountSql = selectSql.toLowerCase().contains("count(");
		// String tableName=isCountSql?"c":"b";
		// select count(*) from t_money_investor_statistics a right join
		// t_money_investor_tradeorder b
		// on b.investorOid =a.investorOid
		// where 1=1 and b.productOid ='226f412530ff4e41bc9b8ae54d36582f'
		// limit 10;
		Query emQuery = null;
		/*
		 * if (!mUAllReq.isFromMimosaProductOnly()) { return emQuery; }
		 */
		if (StringUtils.isBlank(selectSql)) {
			return emQuery;
		}

		StringBuffer sb = new StringBuffer();
		sb.append(selectSql);// 首先添加查询条件
		if (null != mUAllReq.getLockStatus()) {
			sb.append(" and a.cardlockstatus= :CARDLOCKSTATUS ");
		}
		if (StringUtils.isNotBlank(mUAllReq.getProductName())) {
			sb.append("  and b.productOid in (select oid from t_gam_product k where k.name like :PRODUCTNAME ) ");

		}
		if (StringUtils.isNotBlank(mUAllReq.getProductType())) {
			sb.append(" and b.productOid in (select oid from t_gam_product k where k.type = :PRODUCTTYPE) ");
		}
		// List<String> POIDS = null;
		//
		// if (StringUtils.isNotBlank(mUAllReq.getProductName())) {
		// POIDS = queryProductOidByName(mUAllReq);
		// }
		// if (StringUtils.isNotBlank(mUAllReq.getProductType())) {
		//
		// List<String> POIDSByType = queryProductOidsByType(mUAllReq);
		// if (null != POIDS) {
		// POIDS.retainAll(POIDSByType);
		// } else {
		// POIDS = POIDSByType;
		// }
		// }
		//
		// if (null != POIDS) {
		// // 查询type/name对应的所有oids
		// sb.append(" and b.productOid in (:POIDS) ");
		// }

		if (null != mUAllReq.getInvested()) {

			if (mUAllReq.getInvested() == 1) {
				// 投资过
				sb.append("and tmits.totalInvestAmount > 0 ");
			}
			if (mUAllReq.getInvested() == 2) {
				// 未资过
				sb.append("and tmits.totalInvestAmount is null ");
			}
		}
		if (null != mUAllReq.getCInvestAmountStart()) {
			// 在投
			sb.append("and tmits.totalInvestAmount-a.totalRedeemAmount>= :CINVESTAMOUNTSTART ");
		}
		if (null != mUAllReq.getCInvestAmountEND()) {
			// 在投
			sb.append("and tmits.totalInvestAmount-a.totalRedeemAmount<= :CINVESTAMOUNTEND ");
		}
		if (null != mUAllReq.getTotalInvestAmountStart()) {
			// 累投
			sb.append("and tmits.totalInvestAmount>= :TOTALINVESTAMOUNTSTART ");
		}
		if (null != mUAllReq.getTotalInvestAmountEnd()) {
			// 累投
			sb.append("and tmits.totalInvestAmount<= :TOTALINVESTAMOUNTEND ");
		}
		if (null != mUAllReq.getTradeTimeStart()) {
			sb.append("and b.orderTime>= :TRADETIMESTART ");
		}
		// createTimeEnd
		if (null != mUAllReq.getTradeTimeEnd()) {
			sb.append("and b.orderTime<= :TRADETIMEEND ");
		}
		if (null != mUAllReq.getLastTradeTimeStart()) {
			//末次交易时间
			sb.append("and tmits.orderTime>= :LASTTRADETIMESTART ");
		}
		if (null != mUAllReq.getLastTradeTimeEnd()) {
			//末次交易时间
			sb.append("and tmits.orderTime<= :LASTTRADETIMEEND ");
		}
		if (null != mUAllReq.getOrderTypeList()) {
			sb.append("and b.orderType in(:ORDERTYPELIST) ");

		}
		if (null != mUAllReq.getOrderStatusList()) {
			sb.append("and b.orderStatus in(:ORDERSTATUSLIST) ");
		}
		// idList
		if (mUAllReq.isIdListNotEmpty()) {
			sb.append(" and a.investorOid in (:USEROIDS) ");
		}
		if (StringUtils.isNotBlank(mUAllReq.getOrderField())) {
			String orderSequence = "desc";
			if (StringUtils.isNotBlank(mUAllReq.getOrderSequence())) {
				orderSequence = mUAllReq.getOrderSequence();
			}
			sb.append(" order by a.").append(mUAllReq.getOrderField() + " ").append(orderSequence + " ");
		}
		// 分页
		// 复合条件，而且当前库作为全量导入，则不使用分页
		if (!isCountSql && mUAllReq.isNeedPage()) {
			sb.append(" limit ").append((mUAllReq.getPage() - 1) * mUAllReq.getRows()).append(",")
					.append(mUAllReq.getRows());
		}

		emQuery = em.createNativeQuery(sb.toString());
		if (null != mUAllReq.getLockStatus()) {
			emQuery.setParameter("CARDLOCKSTATUS", mUAllReq.getLockStatus());
		}
		if (StringUtils.isNotBlank(mUAllReq.getProductType())) {
			emQuery.setParameter("PRODUCTTYPE", mUAllReq.getProductType());
		}
		if (StringUtils.isNotBlank(mUAllReq.getProductName())) {
			emQuery.setParameter("PRODUCTNAME", mUAllReq.getProductName() + "%");
		}
		// if (StringUtils.isNotBlank(mUAllReq.getProductName()) ||
		// StringUtils.isNotBlank(mUAllReq.getProductType())) {
		//
		// emQuery.setParameter("POIDS", POIDS);
		// }
		if (null != mUAllReq.getCInvestAmountStart()) {
			// 在投
			emQuery.setParameter("CINVESTAMOUNTSTART", mUAllReq.getCInvestAmountStart());
		}
		if (null != mUAllReq.getCInvestAmountEND()) {
			// 在投
			emQuery.setParameter("CINVESTAMOUNTEND", mUAllReq.getCInvestAmountEND());
		}
		if (null != mUAllReq.getTotalInvestAmountStart()) {
			// 累投
			emQuery.setParameter("TOTALINVESTAMOUNTSTART", mUAllReq.getTotalInvestAmountStart());
		}
		if (null != mUAllReq.getTotalInvestAmountEnd()) {
			emQuery.setParameter("TOTALINVESTAMOUNTEND", mUAllReq.getTotalInvestAmountEnd());
			// 累投
		}
		if (null != mUAllReq.getTradeTimeStart()) {
			emQuery.setParameter("TRADETIMESTART", mUAllReq.getTradeTimeStart());
		}
		// createTimeEnd
		if (null != mUAllReq.getTradeTimeEnd()) {
			emQuery.setParameter("TRADETIMEEND", mUAllReq.getTradeTimeEnd());
		}
		if (null != mUAllReq.getLastTradeTimeStart()) {
			emQuery.setParameter("LASTTRADETIMESTART", mUAllReq.getLastTradeTimeStart());
		}
		if (null != mUAllReq.getLastTradeTimeEnd()) {
			emQuery.setParameter("LASTTRADETIMEEND", mUAllReq.getLastTradeTimeEnd());
		}
		if (null != mUAllReq.getOrderTypeList()) {
			emQuery.setParameter("ORDERTYPELIST", mUAllReq.getOrderTypeList());

		}
		if (null != mUAllReq.getOrderStatusList()) {
			emQuery.setParameter("ORDERSTATUSLIST", mUAllReq.getOrderStatusList());
		}
		// idList
		if (mUAllReq.isIdListNotEmpty()) {
			emQuery.setParameter("USEROIDS", mUAllReq.getIdList());
		}
		return emQuery;
	}

	/**
	 * 红包对账查询条件
	 * 
	 * @param cardbalanceReq
	 * @return
	 */
	private Query card_RedpackConditionSql(String selectSql, CardbalanceReq cardbalanceReq) {
		Query emQuery = null;
		// select语句为空情况
		if (StringUtils.isBlank(selectSql)) {
			return emQuery;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(selectSql);// 首先添加查询条件
		if (StringUtils.isNotBlank(cardbalanceReq.getCouponType())) {
			sb.append(" and a.couponType= :COUPONTYPE ");
		}
		if (null != cardbalanceReq.getCardId()) {
			sb.append(" and a.coupons like :CARDID ");
		}
		if (null != cardbalanceReq.getCardIds()) {
			sb.append(" and a.coupons in (:CARDIDS) ");
		}
		if (null != cardbalanceReq.getCreateTimeStart()) {
			sb.append("and a.createTime>= :CREATETIMESTART ");
		}
		// createTimeEnd
		if (null != cardbalanceReq.getCreateTimeEnd()) {
			sb.append("and a.createTime<= :CREATETIMEEND ");
		}
		if (null!=cardbalanceReq.getOrderTypeList()) {
			sb.append("and a.orderType in(:ORDERTYPELIST) ");
			
		}
		if (null!=cardbalanceReq.getOrderStatusList()) {
			sb.append("and a.orderStatus in(:ORDERSTATUSLIST) ");
		}
		if (StringUtils.isNotBlank(cardbalanceReq.getOrderField())) {
			String orderSequence = "desc";
			if (StringUtils.isNotBlank(cardbalanceReq.getOrderSequence())) {
				orderSequence = cardbalanceReq.getOrderSequence();
			}
			sb.append(" order by a.").append(cardbalanceReq.getOrderField() + " ").append(orderSequence + " ");
		}
		if (!selectSql.toLowerCase().contains("count(") && cardbalanceReq.isNeedPage()) {
			sb.append(" limit ").append((cardbalanceReq.getPage() - 1) * cardbalanceReq.getRows()).append(",")
					.append(cardbalanceReq.getRows());
		}
		emQuery = em.createNativeQuery(sb.toString());
		if (StringUtils.isNotBlank(cardbalanceReq.getCouponType())) {
			emQuery.setParameter("COUPONTYPE", cardbalanceReq.getCouponType());
		}
		if (null != cardbalanceReq.getCardId()) {
			emQuery.setParameter("CARDID", "%" + cardbalanceReq.getCardId() + "%");
		}
		if (null != cardbalanceReq.getCardIds()) {
			emQuery.setParameter("CARDIDS", cardbalanceReq.getCardIds());
		}
		if (null != cardbalanceReq.getCreateTimeStart()) {
			emQuery.setParameter("CREATETIMESTART", cardbalanceReq.getCreateTimeStart());
		}
		if (null != cardbalanceReq.getCreateTimeEnd()) {
			emQuery.setParameter("CREATETIMEEND", cardbalanceReq.getCreateTimeEnd());
		}
		if (null!=cardbalanceReq.getOrderTypeList()) {
			emQuery.setParameter("ORDERTYPELIST", cardbalanceReq.getOrderTypeList());
			
		}
		if (null!=cardbalanceReq.getOrderStatusList()) {
			emQuery.setParameter("ORDERSTATUSLIST", cardbalanceReq.getOrderStatusList());
		}
		return emQuery;
	}

	/**
	 * 获取investor红包使用情况
	 */
	public List<Map<String, Object>> getCard_RedpackUsesInfo(CardbalanceReq cardbalanceReq) {
		// 获取卡券类型是红包的订单
		StringBuffer sb = new StringBuffer(40);

		sb.append(
				"select  a.oid,a.orderCode,a.investorOid,a.coupons,a.createTime,a.orderType,a.orderStatus,a.orderAmount,a.productOid,"
						+ "a.payAmount,'used' as usedStatus " + "  from t_money_investor_tradeorder a ");
		sb.append(" where 1=1 ");
		
		Query emQuery = card_RedpackConditionSql(sb.toString(), cardbalanceReq);
		sb = null;
		return CardVoUtil.query2Map(emQuery);
	}

	/**
	 * 获取investor红包使用情况数目
	 */
	public long countCard_RedpackUsesInfo(CardbalanceReq cardbalanceReq) {
		// 获取卡券类型是红包的订单
		// select count(*) from t_money_investor_tradeorder a where
		// a.couponType='tasteCoupon'
		// String cardType = "REDPACK";
		StringBuffer sb = new StringBuffer(40);

		sb.append("select COUNT(1) from t_money_investor_tradeorder a ");
		sb.append(" where 1=1 ");
		Query emQuery = card_RedpackConditionSql(sb.toString(), cardbalanceReq);
		sb = null;
		return CardVoUtil.countNum(emQuery).longValue();

	}

	/**
	 * 获取用户使用红包的订单id
	 * 
	 * @param userOids
	 * @return UserInvedtorOidsReq
	 */
	public List<Map<String, Object>> getInvestOidsByUserOids(UserInvedtorOidsReq userInvedtorOidsReq) {
		String sql = "select distinct a.investorOid,a.orderCode,a.coupons from t_money_investor_tradeorder a  "
				+ " where a.coupons in (:COUPONS)  and a.investorOid in (:USEROIDS) and a.orderType in(:ORDERTYPELIST) and a.orderStatus in(:ORDERSTATUSLIST) ";
		Query emQuery = em.createNativeQuery(sql);
		emQuery.setParameter("COUPONS", userInvedtorOidsReq.getCoupons());
		emQuery.setParameter("USEROIDS", userInvedtorOidsReq.getUserOids());
		emQuery.setParameter("ORDERTYPELIST", userInvedtorOidsReq.getOrderTypeList());
		emQuery.setParameter("ORDERSTATUSLIST", userInvedtorOidsReq.getOrderStatusList());
		return CardVoUtil.query2Map(emQuery);
	}

	/**
	 * 获取全部产品信息，用于缓存
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getProductInfos() {
		String sql = "select a.oid,a.`name`,a.type from t_gam_product a";
		return CardVoUtil.query2Map(sql, em);
	}

	/**
	 * 获取ids产品信息
	 * 
	 * @param oids
	 * @return
	 */
	public List<Map<String, Object>> getProductInfosByOid(Collection<Object> oids) {
		if (null == oids || oids.isEmpty()) {
			return new ArrayList<>();
		}
		String sql = "select a.oid,a.`name`,a.type from t_gam_product a where a.oid in(:OIDS)";
		Query emQuery = em.createNativeQuery(sql);
		emQuery.setParameter("OIDS", oids);
		return CardVoUtil.query2Map(emQuery);
	}

	// select a.oid from t_gam_product a where a.`name`='体验金'
	/**
	 * 根据产品名称获取产品id
	 * 
	 * @param mUAllReq
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> queryProductOidByName(MUAllReq mUAllReq) {
		// System.out.println(name);
		StringBuffer sb = new StringBuffer(40);
		sb.append("select a.oid from t_gam_product a where 1=1 ");
		if (StringUtils.isNotBlank(mUAllReq.getProductName())) {

			sb.append(" and a.`name` like :NAME ");
		}
		Query emQuery = em.createNativeQuery(sb.toString());
		if (StringUtils.isNotBlank(mUAllReq.getProductName())) {

			emQuery.setParameter("NAME", mUAllReq.getProductName() + "%");
		}
		return emQuery.getResultList();

	}

	/**
	 * 根据产品类型获取产品id
	 * 
	 * @param mUAllReq
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> queryProductOidsByType(MUAllReq mUAllReq) {
		StringBuffer sb = new StringBuffer(40);
		sb.append("select a.oid from t_gam_product a where 1=1 ");
		if (StringUtils.isNotBlank(mUAllReq.getProductType())) {

			sb.append(" and a.`type`= :TYPE ");
		}
		Query emQuery = em.createNativeQuery(sb.toString());
		if (StringUtils.isNotBlank(mUAllReq.getProductType())) {
			emQuery.setParameter("TYPE", mUAllReq.getProductType());
		}
		return emQuery.getResultList();
	}

	/**
	 * 获取mimosaStastics的最大更新时间，作为增量缓存数据依据
	 * 
	 * @return
	 */
	public Object getMaxMimosaStatsticsUpdateTime() {
		// select a.channelid from t_wfd_user a GROUP BY a.channelid having
		// a.channelid is not NULL and a.channelid !='';
		StringBuffer sb = new StringBuffer(40);
		sb.append(
				"select DATE_FORMAT(MAX(a.updateTime),'%Y-%m-%d %H:%i:%s') as maxUpdateTime from t_money_investor_statistics a");
		Query emQuery = em.createNativeQuery(sb.toString());
		;
		Map<String, String> map = new HashMap<>();
		if (null != emQuery.getSingleResult()) {
			map.put("updateTime", emQuery.getSingleResult().toString());
			return map;
		}

		return null;
	}

	/**
	 * 查询mimosaStastics的全部数量
	 * 
	 * @param mUAllReq
	 * @return
	 */
	public Long countNumMimosaOnly(MUAllReq mUAllReq) {

		StringBuffer sb = new StringBuffer(40);

		sb.append("select COUNT(*) from t_money_investor_statistics a ");
		sb.append("left join t_money_investor_tradeorder_statistics tmits on tmits.investorOid = a.investorOid ");
		sb.append(" where 1=1 ");
		Query emQuery = sqlConditionOnlyInvestorStatistics(sb.toString(), mUAllReq);
		sb = null;
		return CardVoUtil.countNum(emQuery).longValue();
	}

	/**
	 * 获取只有tradeOrder条件的用户id数量
	 * 
	 * @param mUAllReq
	 * @return
	 */
	public Long countNumProductOnly(MUAllReq mUAllReq) {
		StringBuffer sb = new StringBuffer(40);
		if (mUAllReq.isLastTradeTimeNotNull()) {
			StringBuffer middleSql = new StringBuffer();
			if (null != mUAllReq.getOrderTypeList()) {
				middleSql.append("and orderType in(:ORDERTYPELIST) ");

			}
			if (null != mUAllReq.getOrderStatusList()) {
				middleSql.append("and orderStatus in(:ORDERSTATUSLIST) ");
			}

			// String sql=" select SQL_BUFFER_RESULT count(DISTINCT
			// b.investorOid) "
			// +" from "
			// +" t_money_investor_tradeorder a FORCE
			// INDEX(IDX_INVESTOR_TRADEORDER_orderTime) "
			// +" inner join "
			// +" (select investorOid , max(orderTime) orderTime from
			// t_money_investor_tradeorder FORCE
			// INDEX(IDX_INVESTOR_TRADEORDER_orderTime) "
			// + " where 1=1 "
			// + middleSql
			// + " group by investorOid) b "
			// +" on b.investorOid = a.investorOid "
			// +" where 1=1 "
			// + " and b.orderTime=a.orderTime"
			// ;
			String sql = " select count(DISTINCT a.investorOid) from t_money_investor_tradeorder a "
					+ " inner join t_money_investor_lasttradeorder b " + " on b.investorOid=a.investorOid "
					+ " where 1=1 ";

			sb.append(sql);
		} else {
			sb.append("select COUNT(DISTINCT a.investorOid) from t_money_investor_tradeorder a ");
			sb.append(" where 1=1 ");
		}
		Query emQuery = sqlConditionOnlyProduct(sb.toString(), mUAllReq);
		sb = null;
		return CardVoUtil.countNum(emQuery).longValue();
	}

	/**
	 * 查询大于某个日期的mimosaStastics数量
	 * 
	 * @param timePageReq
	 * @return
	 */
	public Long countNumMimosaOnlyBiggerThan(TimePageReq timePageReq) {
		if (StringUtils.isBlank(timePageReq.getUpdateTime())) {
			return BigInteger.ZERO.longValue();
		}
		StringBuffer sb = new StringBuffer(40);

		sb.append("select COUNT(*) from t_money_investor_statistics a ");
		sb.append(" where 1=1 ");
		sb.append(" and DATE_FORMAT(a.updateTime,'%Y-%m-%d %H:%i:%s')>=");
		sb.append("'" + timePageReq.getUpdateTime() + "' ");

		return CardVoUtil.countNum(sb.toString(), em).longValue();
	}

	/**
	 * 获取查询条件下，同时tradeOrder和mimosa信息的全部数量
	 * 
	 * @param mUAllReq
	 * @return
	 */
	public Long countNumJoinMimosaAndTradeOrder(MUAllReq mUAllReq) {

		StringBuffer sb = new StringBuffer(40);
		if (mUAllReq.isLastTradeTimeNotNull()) {
			StringBuffer middleSql = new StringBuffer();
			if (null != mUAllReq.getOrderTypeList()) {
				middleSql.append("and orderType in(:ORDERTYPELIST) ");

			}
			if (null != mUAllReq.getOrderStatusList()) {
				middleSql.append("and orderStatus in(:ORDERSTATUSLIST) ");
			}
			// String sql=" select SQL_BUFFER_RESULT count(DISTINCT
			// c.investorOid) "
			// +" from t_money_investor_statistics a "
			// +" inner join t_money_investor_tradeorder b FORCE
			// INDEX(IDX_INVESTOR_TRADEORDER_orderTime) "
			// +" on b.investorOid =a.investorOid "
			// +" inner join "
			// +" (select investorOid , max(orderTime) orderTime from
			// t_money_investor_tradeorder "
			// + " where 1=1 "
			// + middleSql
			// + " group by investorOid) c "
			// +" on c.investorOid = b.investorOid "
			// +" where 1=1 "
			// +" and c.orderTime = b.orderTime ";
			String sql = " select count(DISTINCT a.investorOid) from t_money_investor_statistics a "
					+ " inner join t_money_investor_tradeorder b " + " on b.investorOid=a.investorOid "
					+ " inner join  t_money_investor_tradeorder_statistics tmits  on tmits.investorOid = a.investorOid "
					+ " where 1=1 ";

			sb.append(sql);
		} else {
			sb.append("select   count(DISTINCT a.investorOid) from t_money_investor_statistics a ");
			sb.append(" inner join t_money_investor_tradeorder b ");
			sb.append(" on  b.investorOid =a.investorOid ");
			sb.append(" inner join t_money_investor_tradeorder_statistics tmits ");
			sb.append(" on  tmits.investorOid = a.investorOid ");
			sb.append(" where 1=1 ");
		}
		Query emQuery = sqlConditionProduct(sb.toString(), mUAllReq);
		sb = null;
		return CardVoUtil.countNum(emQuery).longValue();
	}

	/**
	 * 获取mimosaStastics的全部相关结果集
	 * 
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsOnlyMimosa(MUAllReq mUAllReq) {
		// sql语句最好分离 方便count和query
		StringBuffer sb = new StringBuffer(40);

		sb.append(
				"select a.investorOid,if(tmits.totalInvestAmount>0,1,2) invested ,tmits.totalInvestCount,tmits.totalInvestAmount,IF(( tmits.totalInvestAmount-a.totalRedeemAmount)<0,0.0000,( tmits.totalInvestAmount-a.totalRedeemAmount)) as investingAmout,DATE_FORMAT(a.updateTime,'%Y-%m-%d %H:%i:%s')  as mimosaUpdateTime");
		sb.append(" from t_money_investor_statistics a   ");
		sb.append("left join t_money_investor_tradeorder_statistics tmits on tmits.investorOid = a.investorOid ");
		sb.append(" where 1=1 ");

		Query emQuery = sqlConditionOnlyInvestorStatistics(sb.toString(), mUAllReq);
		sb = null;
		return CardVoUtil.query2Map(emQuery);

	}

	/**
	 * 获取大于某个时间的mimosaStastics的结果集
	 * 
	 * @param timePageReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsOnlyMimosaBiggerThan(TimePageReq timePageReq) {
		// sql语句最好分离 方便count和query
		StringBuffer sb = new StringBuffer(40);

		sb.append(
				"select a.investorOid,if(tmits.totalInvestAmount>0,1,2) invested ,tmits.totalInvestCount,tmits.totalInvestAmount,IF(( tmits.totalInvestAmount-a.totalRedeemAmount)<0,0.0000,( tmits.totalInvestAmount-a.totalRedeemAmount)) as investingAmout,DATE_FORMAT(a.updateTime,'%Y-%m-%d %H:%i:%s')  as mimosaUpdateTime");
		sb.append(" from t_money_investor_statistics a   ");
		sb.append(" left join t_money_investor_tradeorder_statistics tmits   ");
		sb.append(" where 1=1 ");
		sb.append(" and DATE_FORMAT(a.updateTime,'%Y-%m-%d %H:%i:%s')>=");
		sb.append("'" + timePageReq.getUpdateTime() + "' ");
		sb.append(" limit ").append((timePageReq.getPage() - 1) * timePageReq.getRows()).append(",")
				.append(timePageReq.getRows());
		return CardVoUtil.query2Map(sb.toString(), em);

	}

	/**
	 * mimosa组合条件的结果集
	 * 
	 * @param mUAllReq
	 * @return
	 */
	// public List<Map<String, Object>>
	// query2MapsJoinMimosaAndTradeOrder(MUAllReq mUAllReq) {
	// // sql语句最好分离 方便count和query
	// StringBuffer sb = new StringBuffer(40);
	//
	// sb.append(
	// "select DISTINCT a.investorOid,if(a.totalInvestCount>0,1,2) invested
	// ,a.totalInvestCount,a.totalInvestAmount,(
	// a.totalInvestAmount-a.totalRedeemAmount) investingAmout");
	// sb.append(" from t_money_investor_statistics a ");
	// sb.append(" inner join t_money_investor_tradeorder b ");
	// sb.append(" on b.investorOid =a.investorOid ");
	// sb.append(" where 1=1 ");
	//
	// Query emQuery = sqlConditionProduct(sb.toString(), mUAllReq);
	// sb = null;
	// return CardVoUtil.query2Map(emQuery);
	//
	// }

	/**
	 * 获取mimosaStastics的查询条件下的用户ids
	 * 
	 * @param mUAllReq
	 * @return
	 */
	public List<String> query2IdListOnlyMimosa(MUAllReq mUAllReq) {
		StringBuffer sb = new StringBuffer(40);
		sb.append("select a.investorOid from t_money_investor_statistics a ");
		sb.append("left join t_money_investor_tradeorder_statistics tmits on tmits.investorOid = a.investorOid ");
		sb.append(" where 1=1 ");
		Query emQuery = sqlConditionOnlyInvestorStatistics(sb.toString(), mUAllReq);
		sb = null;
		return CardVoUtil.query2List(emQuery);
	}

	/**
	 * 查询只有tradeOrder信息的用户ids
	 * 
	 * @param mUAllReq
	 * @return
	 */
	public List<String> query2IdListOnlyProduct(MUAllReq mUAllReq) {
		StringBuffer sb = new StringBuffer(80);
		if (mUAllReq.isLastTradeTimeNotNull()) {
			// StringBuffer middleSql=new StringBuffer();
			// if (null!=mUAllReq.getOrderTypeList()) {
			// middleSql.append("and orderType in(:ORDERTYPELIST) ");
			//
			// }
			// if (null!=mUAllReq.getOrderStatusList()) {
			// middleSql.append("and orderStatus in(:ORDERSTATUSLIST) ");
			// }

			// String sql=" select distinct b.investorOid "
			// +" from "
			// +" t_money_investor_tradeorder a FORCE
			// INDEX(IDX_INVESTOR_TRADEORDER_orderTime) "
			// +" inner join "
			// +" (select investorOid , max(orderTime) orderTime from
			// t_money_investor_tradeorder FORCE
			// INDEX(IDX_INVESTOR_TRADEORDER_orderTime) "
			// + " where 1=1 "
			// + middleSql
			// + " group by investorOid) b "
			// +" on b.investorOid = a.investorOid "
			// +" where 1=1 "
			// + " and b.orderTime=a.orderTime"
			// ;
			// String sql=" select DISTINCT a.investorOid "
			// +" from t_money_investor_tradeorder a "
			// +" where 1=1 "
			// +" and not exists (select investorOid from
			// t_money_investor_tradeorder "
			// + " where investorOid=a.investorOid and orderTime> a.orderTime)
			// ";
			String sql = " select DISTINCT a.investorOid from t_money_investor_tradeorder a "
					+ " inner join t_money_investor_lasttradeorder b " + " on b.investorOid=a.investorOid "
					+ " where 1=1 ";
			sb.append(sql);
		} else {

			sb.append("select DISTINCT a.investorOid from t_money_investor_tradeorder a ");
			sb.append(" where 1=1 ");
		}
		Query emQuery = sqlConditionOnlyProduct(sb.toString(), mUAllReq);
		sb = null;
		return CardVoUtil.query2List(emQuery);
	}

	/**
	 * 获取mimosa组合的查询条件下的用户ids
	 * 
	 * @param mUAllReq
	 * @return
	 */
	public List<String> query2IdListJoinMimosaAndTradeOrder(MUAllReq mUAllReq) {
		StringBuffer sb = new StringBuffer(80);
		if (mUAllReq.isLastTradeTimeNotNull()) {
			// StringBuffer middleSql=new StringBuffer();
			// if (null!=mUAllReq.getOrderTypeList()) {
			// middleSql.append("and orderType in(:ORDERTYPELIST) ");
			//
			// }
			// if (null!=mUAllReq.getOrderStatusList()) {
			// middleSql.append("and orderStatus in(:ORDERSTATUSLIST) ");
			// }
			// String sql =" select distinct b.investorOid "
			// +" from t_money_investor_statistics a "
			// +" inner join t_money_investor_tradeorder b "
			// +" on b.investorOid =a.investorOid "
			// +" where 1=1 "
			// +" and not exists (select investorOid from
			// t_money_investor_tradeorder "
			// + " where investorOid=b.investorOid and orderTime>b.orderTime) ";
			// String sql=" select DISTINCT c.investorOid "
			// +" from t_money_investor_statistics a "
			// +" inner join t_money_investor_tradeorder b FORCE
			// INDEX(IDX_INVESTOR_TRADEORDER_orderTime) "
			// +" on b.investorOid =a.investorOid "
			// +" inner join "
			// +" (select investorOid , max(orderTime) orderTime from
			// t_money_investor_tradeorder FORCE
			// INDEX(IDX_INVESTOR_TRADEORDER_orderTime) "
			// + " where 1=1 "
			// + middleSql
			// + " group by investorOid) c "
			// +" on c.investorOid = b.investorOid "
			// +" where 1=1 "
			// +" and c.orderTime = b.orderTime ";
			String sql = " select DISTINCT a.investorOid from t_money_investor_statistics a "
					+ " inner join t_money_investor_tradeorder b " + " on b.investorOid=a.investorOid "
					+ " inner join  t_money_investor_tradeorder_statistics tmits on tmits.investorOid = a.investorOid "
					+ " where 1=1 ";
			sb.append(sql);

		} else {

			sb.append("select DISTINCT a.investorOid from t_money_investor_statistics a ");
			sb.append(" inner join t_money_investor_tradeorder b ");
			sb.append(" on  b.investorOid =a.investorOid ");
			sb.append(" inner join t_money_investor_tradeorder_statistics tmits ");
			sb.append(" on  tmits.investorOid = a.investorOid ");
			sb.append(" where 1=1 ");
		}
		Query emQuery = sqlConditionProduct(sb.toString(), mUAllReq);
		sb = null;
		return CardVoUtil.query2List(emQuery);
	}

	/**
	 * 获取是否是新手标和竞猜宝的产品oids
	 * 
	 * @return
	 */
	public List<String> getInvalidProductIds() {
		String sql = "select t1.oid from T_GAM_PRODUCT t1 where  t1.productLabel='freshman' or t1.guessOid is not null";
		return CardVoUtil.query2List(sql, em);
	}

	/**
	 * 排除 判断是否定期产品 新手标和竞猜宝
	 * 
	 * @param productOid
	 * @return
	 */
	public Object isDepositProduct(Object productOid) {
		String sql = "select count(*) from T_GAM_PRODUCT t1 where  t1.productLabel!='freshman' and t1.guessOid is  null "
				+ "and t1.type='PRODUCTTYPE_01' and t1.oid='" + productOid + "'";
		long count = CardVoUtil.countNum(sql, em).longValue();
		if (count > 0) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * @desc 根据产品类型和状态分页获取产品明细
	 * @author hy
	 * @date 2017.5.11
	 */
	public List<Map<String, Object>> getProductByType(ProductReq req) {
		StringBuffer sb = new StringBuffer(80);
		sb.append(" select t1.oid,t1.name,t1.fullName,t1.expAror,DATE_FORMAT(t1.createTime,'%Y-%m-%d %H:%i:%s') "
				+ "createTime,t1.raisedTotalNumber from T_GAM_PRODUCT t1 where 1=1 and t1.productLabel!='freshman' and t1.guessOid is null ");

		if (StringUtils.isNotBlank(req.getType())) {
			sb.append(" and t1.type = :type");
		}
		if (req.getProductStatusList() != null && req.getProductStatusList().size() > 0) {
			sb.append(" and t1.state in (:states)");
		}
		if (req.getOids() != null && req.getOids().size() > 0) {
			sb.append(" and t1.oid in (:oids)");
		}
		sb.append(" limit ").append((req.getPage() - 1) * req.getRows()).append(",").append(req.getRows());
		Query emQuery = em.createNativeQuery(sb.toString());
		if (StringUtils.isNotBlank(req.getType())) {
			emQuery.setParameter("type", req.getType());
		}
		if (req.getProductStatusList() != null && req.getProductStatusList().size() > 0) {
			emQuery.setParameter("states", req.getProductStatusList());
		}
		if (req.getOids() != null && req.getOids().size() > 0) {
			emQuery.setParameter("oids", req.getOids());
		}
		sb = null;
		return CardVoUtil.query2Map(emQuery);
	}

	/**
	 * @desc 根据产品类型和状态分页获取产品总数量
	 * @author hy
	 * @date 2017.5.11
	 */
	public long getProductCount(ProductReq req) {
		StringBuffer sb = new StringBuffer(80);
		sb.append(" select count(1) from T_GAM_PRODUCT t1 where 1=1 ");
		if (StringUtils.isNotBlank(req.getType())) {
			sb.append(" and t1.type = :type");
		}
		if (req.getProductStatusList() != null && req.getProductStatusList().size() > 0) {
			sb.append(" and t1.state in (:states)");
		}
		if (req.getOids() != null && req.getOids().size() > 0) {
			sb.append(" and t1.oid in (:oids)");
		}
		Query emQuery = em.createNativeQuery(sb.toString());
		if (StringUtils.isNotBlank(req.getType())) {
			emQuery.setParameter("type", req.getType());
		}
		if (req.getProductStatusList() != null && req.getProductStatusList().size() > 0) {
			emQuery.setParameter("states", req.getProductStatusList());
		}
		if (req.getOids() != null && req.getOids().size() > 0) {
			emQuery.setParameter("oids", req.getOids());
		}
		sb = null;
		return CardVoUtil.countNum(emQuery).longValue();
	}

	/**
	 * @desc 根据产品oid查询标签
	 * @author hy
	 * @date 2017.5.11
	 */
	public List<Map<String, Object>> getProducLabletByProductOid(List<String> productOids) {
		StringBuffer sb = new StringBuffer(80);
		sb.append(
				" select t1.productOid,t2.oid,t2.labelName,t2.labelType from  T_MONEY_PLATFORM_LABEL_PRODUCT t1 left join T_MONEY_PLATFORM_LABEL t2 on t1.labelOid = t2.oid where 1=1 ");
		if (productOids != null && productOids.size() > 0) {
			sb.append(" and t1.productOid in (:productOids)");
		}
		Query emQuery = em.createNativeQuery(sb.toString());
		if (productOids != null && productOids.size() > 0) {
			emQuery.setParameter("productOids", productOids);
		}
		sb = null;
		return CardVoUtil.query2Map(emQuery);
	}
	
	/**
	 * @desc 根据产品oid查询符合的产品
	 * @author huyong
	 * @date 2017.12.12
	 */
	public List<Map<String, Object>> findProductByOid(String productOid,String channelId) {
		StringBuffer sb = new StringBuffer(80);
		sb.append("select t1.oid productOid,t1.`name` productName,ifnull(t1.investMin,0) investMin,ifnull(t1.setupDate,'') setupDate,ifnull(t1.durationPeriodDays,0) durationPeriodDays,")
		.append(" case when t1.type = 'PRODUCTTYPE_01' and t1.productLabel = 'freshman' then 'freshman' ")
		.append(	" 	   when t1.type = 'PRODUCTTYPE_01' and t1.isActivityProduct = 1 then 'activity' ")
		.append(	" 	   when t1.type = 'PRODUCTTYPE_02' and (select count(1) from t_gam_income_reward t2 where t2.productOid = t1.oid) > 0 then 'increment' ")
		.append(" else lower(t1.type) end as type,")
		.append(" t1.expAror,t1.expArorSec,t1.expectedArrorDisp")
		.append(" from t_gam_product t1, t_gam_product_channel t2, t_money_platform_channel t3 ")
		.append(" where t1.oid = :oid and t3.oid = :channelId and t1.oid = t2.productOid and t2.channelOid = t3.oid and t2.marketState = 'ONSHELF' ")
		.append(" and ((t1.state = 'RAISING' and t1.type = 'PRODUCTTYPE_01') or (t1.state = 'DURATIONING' and t1.type = 'PRODUCTTYPE_02')) and t1.oid != '0e4ee8ead74241de90f82e06f987ef8d' and t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume > 0");
		Query emQuery = em.createNativeQuery(sb.toString());
		emQuery.setParameter("oid", productOid);
		emQuery.setParameter("channelId", channelId);
		sb = null;
		return CardVoUtil.query2Map(emQuery);
	}
	
	/**
	 * @desc 根据条件查询符合的产品
	 * @author huyong
	 * @date 2017.12.12
	 */
	public List<Map<String, Object>> placeProducts(ProductPlaceEntity product) {
		StringBuffer sb = new StringBuffer(80);
		sb.append("select t1.oid productOid,t1.`name` productName,ifnull(t1.investMin,0) investMin,ifnull(t1.setupDate,'') setupDate,ifnull(t1.durationPeriodDays,0) durationPeriodDays,")
		.append(" case when t1.productLabel = 'freshman' then 'freshman' ")
		.append(	" 	   when t1.isActivityProduct = 1 then 'activity' ")
		.append(" else lower(t1.type) end as type,")
		.append(" t1.expAror,t1.expArorSec,t1.expectedArrorDisp ")
		.append(" from t_gam_product t1, t_gam_product_channel t2, t_money_platform_channel t3 ")
		.append(" where t1.type = 'PRODUCTTYPE_01' and t3.oid = :channelId and t1.oid = t2.productOid and t2.channelOid = t3.oid and t2.marketState = 'ONSHELF' ")
		.append(" and t1.state = 'RAISING' and t1.raisedTotalNumber - t1.collectedVolume - t1.lockCollectedVolume > 0");
		if (product.getHasBaofengbao() == 1 && product.getHasFreshMan() == 0 && product.getHasZeroBuy() == 0) {
			sb.append(" and t1.guessOid is null and t1.productLabel != 'freshman' and t1.isActivityProduct != 1");
		}else if(product.getHasBaofengbao() == 1 && product.getHasFreshMan() == 1 && product.getHasZeroBuy() == 0) {
			sb.append(" and t1.guessOid is null and t1.isActivityProduct != 1");
		}else if (product.getHasBaofengbao() == 1 && product.getHasFreshMan() == 1 && product.getHasZeroBuy() == 1) {
			sb.append(" and t1.guessOid is null");
		}else if(product.getHasBaofengbao() == 0 && product.getHasFreshMan() == 1 && product.getHasZeroBuy() == 0) {
			sb.append(" and t1.productLabel = 'freshman'");
		}else if(product.getHasBaofengbao() == 0 && product.getHasFreshMan() == 1 && product.getHasZeroBuy() == 1) {
			sb.append(" and (t1.productLabel = 'freshman' or t1.isActivityProduct = 1)");
		}else if(product.getHasBaofengbao() == 0 && product.getHasFreshMan() == 0 && product.getHasZeroBuy() == 1) {
			sb.append(" and t1.isActivityProduct = 1");
		}else if(product.getHasBaofengbao() == 1 && product.getHasFreshMan() == 0 && product.getHasZeroBuy() == 1) {
			sb.append(" and t1.guessOid is null and t1.productLabel != 'freshman'");
		}
		if (product.getOrderBy() == ProductPlaceEntity.EXPAROR_ARRORDISP_ASC) {
			sb.append(" order by if(t1.expAror > IFNULL(t1.expectedArrorDisp,0),t1.expAror,IFNULL(t1.expectedArrorDisp,0)) desc");
		}else if(product.getOrderBy() == ProductPlaceEntity.DURATIONPERIOD_DESC) {
			sb.append(" order by t1.durationPeriodDays desc");
		}else if(product.getOrderBy() == ProductPlaceEntity.DURATIONPERIOD_ASC) {
			sb.append(" order by t1.durationPeriodDays asc");
		}
		if(product.getMaxNum() != 0) {
			sb.append(" limit :maxNum");
		}
		Query emQuery = em.createNativeQuery(sb.toString());
		emQuery.setParameter("channelId", product.getChannelId());
		if(product.getMaxNum() != 0) {
			emQuery.setParameter("maxNum", product.getMaxNum());
		}
		sb = null;
		return CardVoUtil.query2Map(emQuery);
	}

	public String getProductCodeByOid(String productOid) {
		StringBuffer sb = new StringBuffer();
		sb.append("select productCode from t_gam_product where productOid= :oid");
		Query emQuery = em.createNativeQuery(sb.toString());
		emQuery.setParameter("oid", productOid);
		sb = null;
		return CardVoUtil.query2List(emQuery).get(0).toString();
	}

	/**
	 * @desc 查询赎回订单
	 * @author huyong
	 * @date 2018.4.12
	 */
	public List<Map<String, Object>> getBfPlusRedeemList(java.util.Date payDate) {
		StringBuffer sb = new StringBuffer(80);
		sb.append("select t1.oid,t1.orderCode,t1.orderAmount,t1.investorOid,t1.productOid,t1.createTime " +
				"FROM T_MONEY_INVESTOR_TRADEORDER t1 INNER JOIN T_MONEY_INVESTOR_PLUS_REDEEM r ON r.oid = t1.oid  " +
				"where r.payDate = :payDate AND  t1.orderStatus='confirmed' AND status = 0 " +
				"order by r.createDate DESC limit 20");
		Query emQuery = em.createNativeQuery(sb.toString());
		emQuery.setParameter("payDate", payDate);
//		emQuery.setParameter("createDate", createDate);
		sb = null;
		return CardVoUtil.query2Map(emQuery);
	}

}
