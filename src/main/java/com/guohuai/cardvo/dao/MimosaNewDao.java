package com.guohuai.cardvo.dao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.google.common.base.Objects;
import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.cardvo.util.CardVoUtil;

/**
 * 最新获取用户信息形式
 * 
 * @author yujianlong
 *
 */
@Repository
public class MimosaNewDao {

	/**
	 * 绑定jpa EntityManager
	 */
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 计算数量
	 * @param mUAllReq
	 * @return
	 */
	public Long countAll(MUAllReq mUAllReq){
		String selectSql="select COUNT(*) ";
		Query emQuery =CommonQuery(selectSql, mUAllReq, 0, false);
		return CardVoUtil.countNum(emQuery).longValue();
	}
	/**
	 * 计算所有锁定用户的数量
	 * @param mUAllReq
	 * @return
	 */
	public Long countAllLocked(MUAllReq mUAllReq){
		String selectSql="select COUNT(*) ";
		Query emQuery =CommonQuery(selectSql, mUAllReq, 1, false);
		return CardVoUtil.countNum(emQuery).longValue();
	}
	/**
	 * 获取全部userid列表
	 * @param mUAllReq
	 * @return
	 */
	public  List<String> queryIdList(MUAllReq mUAllReq){
		String selectSql="select mib.userOid ";
		Query emQuery =CommonQuery(selectSql, mUAllReq, 0, false);
		return CardVoUtil.query2List(emQuery);
	}
	/**
	 * 获取全部锁定userid列表
	 * @param mUAllReq
	 * @return
	 */
	public  List<String> queryIdListOnlocked(MUAllReq mUAllReq){
		String selectSql="select mib.userOid ";
		Query emQuery =CommonQuery(selectSql, mUAllReq, 1, false);
		return CardVoUtil.query2List(emQuery);
	}
	/**查询部分字段
	 * @param mUAllReq
	 * @return
	 */
	public  List<Map<String, Object>> query2MapsSome(MUAllReq mUAllReq){
		String selectSql="select mib.userOid,mib.realName as name,mib.phoneNum as phoneNo "
				+" , if(mib.registerChannelId is not NULL and mib.registerChannelId !='',mib.registerChannelId ,'other')  as channelid,mib.createTime,IF(mib.idNum is null,0,1) as bindedcard  "
				+ ",mib.cardlockstatus as isLock ";
		boolean f1=isNotNeedQueryPublishHold(mUAllReq);
		boolean f2=isNotNeedQueryTradeStastics(mUAllReq);
		if (!f1) {
			selectSql+=" ,mphnew.investingAmout ";
		}
		if (!f2) {
			selectSql+=" ,tmits.totalInvestAmount,tmits.totalInvestCount ";
		}
		Query emQuery =CommonQuery(selectSql, mUAllReq, 0, false);
		return CardVoUtil.query2Map(emQuery);
	}
	/**查询锁定用户部分字段
	 * @param mUAllReq
	 * @return
	 */
	public  List<Map<String, Object>> query2MapsSomeOnlocked(MUAllReq mUAllReq){
		String selectSql="select mib.userOid,mib.realName as name,mib.phoneNum as phoneNo "
				+" ,if(mib.registerChannelId is not NULL and mib.registerChannelId !='',mib.registerChannelId ,'other')  as channelid,mib.createTime,IF(mib.idNum is null,0,1) as bindedcard  "
				+ ",mib.cardlockstatus as isLock ";
		boolean f1=isNotNeedQueryPublishHold(mUAllReq);
		boolean f2=isNotNeedQueryTradeStastics(mUAllReq);
		if (!f1) {
			selectSql+=" ,mphnew.investingAmout ";
		}
		if (!f2) {
			selectSql+=" ,tmits.totalInvestAmount,tmits.totalInvestCount ";
		}
		Query emQuery =CommonQuery(selectSql, mUAllReq, 1, false);
		return CardVoUtil.query2Map(emQuery);
	}
	/**查全部字段
	 * @param mUAllReq
	 * @return
	 */
	public  List<Map<String, Object>> query2MapsAll(MUAllReq mUAllReq){
//		sequence
//		userOid
//		name
//		phoneNo
//		channelid
//		createTime
//		bindedcard
//		totalInvestAmount
//		investingAmout
//		tags
//		isLock
		String selectSql="select mib.userOid,mib.realName as name,mib.phoneNum as phoneNo "
				+" ,if(mib.registerChannelId is not NULL and mib.registerChannelId !='',mib.registerChannelId ,'other')  as channelid,mib.createTime,IF(mib.idNum is null,0,1) as bindedcard  "
				+",tmits.totalInvestAmount,tmits.totalInvestCount"
				+ ",mphnew.investingAmout,mib.cardlockstatus as isLock ";
		Query emQuery =CommonQuery(selectSql, mUAllReq, 0, true);
		return CardVoUtil.query2Map(emQuery);
	}
	/**查锁定用户全部字段
	 * @param mUAllReq
	 * @return
	 */
	public  List<Map<String, Object>> query2MapsAllOnlocked(MUAllReq mUAllReq){
		String selectSql="select mib.userOid,mib.realName as name,mib.phoneNum as phoneNo "
				+" ,mib.registerChannelId as channelid,mib.createTime,IF(mib.idNum is null,0,1) as bindedcard  "
				+",tmits.totalInvestAmount,tmits.totalInvestCount"
				+ ",mphnew.investingAmout,mib.cardlockstatus as isLock ";
		Query emQuery =CommonQuery(selectSql, mUAllReq, 1, true);
		return CardVoUtil.query2Map(emQuery);
	}
	
	/**
	 * 持仓表单独查询。
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsOnPublishHoldByUserIds(MUAllReq mUAllReq){
		StringBuilder sbTable=new StringBuilder();
		Map<String, Object> paras = new HashMap<>();
		sbTable.append("  SELECT  mph.investorOid investorOid, ")
		.append(" SUM(mph.holdVolume) investingAmout ")
		.append(" FROM t_money_publisher_hold mph LEFT JOIN t_gam_product gp ON mph.productOid = gp.oid ")
		.append(" WHERE gp.code != 'BFJRTYJLC'  ");
		if (mUAllReq.isIdListNotEmpty()) {
			if (Objects.equal(mUAllReq.getRows(), mUAllReq.getIdList().size())) {
				throw new RuntimeException("传入id参数错误");
			}
			sbTable.append(" and  mph.investorOid in (:USEROIDS) ");
			paras.put("USEROIDS", mUAllReq.getIdList());
		}
		sbTable.append(" GROUP BY mph.investorOid  ");
		
		Query emQuery = em.createNativeQuery(sbTable.toString());
		paras.forEach(emQuery::setParameter);
		return CardVoUtil.query2Map(emQuery);
	}
	/**
	 * 中间表单独查询
	 * @param mUAllReq
	 * @return
	 */
	public List<Map<String, Object>> query2MapsOnTradeStasticsByUserIds(MUAllReq mUAllReq){
		StringBuilder sbTable=new StringBuilder();
		Map<String, Object> paras = new HashMap<>();
		sbTable.append("  SELECT  tmits.investorOid, ")
		.append(" tmits.totalInvestAmount,tmits.totalInvestCount,tmits.lastTradeTime ")
		.append(" FROM t_money_investor_tradeorder_statistics tmits ");
		
		if (mUAllReq.isIdListNotEmpty()) {
			if (Objects.equal(mUAllReq.getRows(), mUAllReq.getIdList().size())) {
				throw new RuntimeException("传入id参数错误");
			}
			sbTable.append(" where  tmits.investorOid in (:USEROIDS) ");
			paras.put("USEROIDS", mUAllReq.getIdList());
		}
		Query emQuery = em.createNativeQuery(sbTable.toString());
		paras.forEach(emQuery::setParameter);
		return CardVoUtil.query2Map(emQuery);
	}
	
	
	/**
	 * 通用query语句
	 * @param selectSql
	 * @param mUAllReq
	 * @param lockType 非锁定0 锁定1
	 * @param needJoin 是否强制连表 默认要给false
	 * @return
	 */
	public Query CommonQuery(String selectSql, MUAllReq mUAllReq,int lockType,boolean needJoin){
		if (StringUtils.isBlank(selectSql)) {
			return null;
		}
		boolean isCountSql = selectSql.toLowerCase().contains("count(");
		StringBuilder sqlAll = new StringBuilder();
		Map<String, Object> paras = new HashMap<>();
		sqlAll.append(selectSql)
				.append(CommonSql(paras, mUAllReq, lockType,needJoin));
		if (!isCountSql) {
			sqlAll.append(" limit ").append((mUAllReq.getPage() - 1) * mUAllReq.getRows()).append(",")
			.append(mUAllReq.getRows());
		}
		
		Query emQuery = em.createNativeQuery(sqlAll.toString());
		paras.forEach(emQuery::setParameter);
		return emQuery;
	}
	
	/**
	 * 生成全部sql
	 * @param paras
	 * @param mUAllReq
	 * @param lockType
	 * @return
	 */
	private StringBuilder CommonSql(Map<String, Object> paras, MUAllReq mUAllReq,int lockType,boolean needJoin){
		StringBuilder gSql=new StringBuilder();
		StringBuilder sbConditionOuter=new StringBuilder();
		
		sbConditionOuter.append(" where mib.cardlockstatus="+lockType+" ");
		//至少要查baseaccount
		gSql.append(baseAccountSql(paras, mUAllReq, sbConditionOuter));
		gSql.append(publishHoldSql(paras, mUAllReq, sbConditionOuter,needJoin));
		gSql.append(tradeStasticsSql(paras, mUAllReq, sbConditionOuter,needJoin));
		
		gSql.append(sbConditionOuter);
		return gSql;
	}
	
	/**
	 * baseAccount查询
	 * 表名 mib
	 * @param paras
	 * @param mUAllReq
	 * @param sbConditionOuter
	 * @return
	 */
	private StringBuilder baseAccountSql(Map<String, Object> paras, MUAllReq mUAllReq,StringBuilder sbConditionOuter){
		StringBuilder sbTable=new StringBuilder();
		
			sbTable.append("  FROM t_money_investor_baseaccount mib ");
			//channelID 渠道
			if (null!=mUAllReq.getChannelIds()&&!mUAllReq.getChannelIds().isEmpty()) {
				sbConditionOuter.append(" and (mib.registerChannelId in (:CHANNELIDS) ");
				if(mUAllReq.getChannelIds().contains("other")){
					sbConditionOuter.append(" or mib.registerChannelId is null or mib.registerChannelId = '' ");
				}
				sbConditionOuter.append(" ) ");
				paras.put("CHANNELIDS", mUAllReq.getChannelIds());
			}
			//createTimeStart
			if (null!=mUAllReq.getCreateTimeStart()) {
				sbConditionOuter.append("and mib.createTime>= :CREATETIMESTART ");
				paras.put("CREATETIMESTART", mUAllReq.getCreateTimeStart());
			}
			//createTimeEnd
			if (null!=mUAllReq.getCreateTimeEnd()) {
				sbConditionOuter.append("and mib.createTime<= :CREATETIMEEND ");
				paras.put("CREATETIMEEND", mUAllReq.getCreateTimeEnd());
			}
			//是否投资过 1投资过，0没投资过
			if (null!=mUAllReq.getInvested()) {
				String freshman=Objects.equal(mUAllReq.getInvested(), 1)?"no":"yes";
				sbConditionOuter.append("and mib.isFreshman= :INVESTED ");
				paras.put("INVESTED", freshman);
			}
			//手机号
			if (StringUtils.isNotBlank(mUAllReq.getPhoneNumb())) {
				sbConditionOuter.append(" and  mib.phoneNum like :PHONENUMB  ");
				paras.put("PHONENUMB", mUAllReq.getPhoneNumb()+"%");
			}
			//用户名
			if (StringUtils.isNotBlank(mUAllReq.getFullName())) {
				sbConditionOuter.append(" and mib.realName like :FULLNAME ");
				paras.put("FULLNAME", mUAllReq.getFullName()+"%");
			}
		
		return sbTable;
	}
	/**
	 * 是否不需要查持仓表
	 * @param mUAllReq
	 * @return
	 */
	private boolean isNotNeedQueryPublishHold(MUAllReq mUAllReq){
		return Stream.of(mUAllReq.getProductType(),
				mUAllReq.getProductName(),
				mUAllReq.getCInvestAmountStart(),
				mUAllReq.getCInvestAmountEND())
				.allMatch(t->{return null==t||Objects.equal(t, "");});
	}
	/**专门针对持仓表
	 * @param paras
	 * @param mUAllReq
	 * 表名 mphnew
	 * @return
	 */
	private StringBuilder publishHoldSql(Map<String, Object> paras, MUAllReq mUAllReq,StringBuilder sbConditionOuter,boolean needJoin){
		StringBuilder sbTable=new StringBuilder();
		boolean f=isNotNeedQueryPublishHold(mUAllReq);
		if (!f||needJoin) {
			StringBuilder innerCondition=new StringBuilder();
			if (StringUtils.isNoneBlank(mUAllReq.getProductType())) {
				innerCondition.append(" AND gp.type = :PRODUCTTYPE   ");
				paras.put("PRODUCTTYPE", mUAllReq.getProductType());
			}
			if (StringUtils.isNoneBlank(mUAllReq.getProductName())) {
				innerCondition.append(" AND gp.name like :PRODUCTNAME   ");
				paras.put("PRODUCTNAME", mUAllReq.getProductName() + "%");
			}
			
			sbTable
			.append(" INNER JOIN (SELECT  mph.investorOid investorOid, ")
			.append(" SUM(mph.holdVolume) investingAmout ")
			.append(" FROM t_money_publisher_hold mph LEFT JOIN t_gam_product gp ON mph.productOid = gp.oid ")
			.append(" WHERE gp.code != 'BFJRTYJLC'  ")
			.append(innerCondition)
			.append(" GROUP BY mph.investorOid  ")
			.append(" ) mphnew ON mib.oid = mphnew.investorOid  ");
			//设置
			if (null!=mUAllReq.getCInvestAmountStart()) {
				boolean isZero=mUAllReq.getCInvestAmountStart().equals(BigDecimal.ZERO);
				if (isZero) {
					sbConditionOuter.append(" and (mphnew.investingAmout>= :CINVESTAMOUNTSTART or mphnew.investingAmout is null)");
				}else{
					sbConditionOuter.append(" and mphnew.investingAmout>=:CINVESTAMOUNTSTART");
				}
				paras.put("CINVESTAMOUNTSTART", mUAllReq.getCInvestAmountStart());
			}
			if (null!=mUAllReq.getCInvestAmountEND()) {
				boolean isZero=mUAllReq.getCInvestAmountEND().equals(BigDecimal.ZERO);
				if (isZero) {
					sbConditionOuter.append(" and (mphnew.investingAmout<= :CINVESTAMOUNTEND or mphnew.investingAmout is null)");
				}else{
					sbConditionOuter.append(" and mphnew.investingAmout<= :CINVESTAMOUNTEND");
				}
				paras.put("CINVESTAMOUNTEND", mUAllReq.getCInvestAmountEND());
			}
		}
		return sbTable;
	}
	
	/**
	 * 是否不需要查中间表
	 * @param mUAllReq
	 * @return
	 */
	private boolean isNotNeedQueryTradeStastics(MUAllReq mUAllReq){
		return Stream.of(mUAllReq.getTotalInvestAmountStart(),
				mUAllReq.getTotalInvestAmountEnd(),
				mUAllReq.getLastTradeTimeStart(),
				mUAllReq.getLastTradeTimeEnd())
				.allMatch(t->{return null==t;});
	}
	/**
	 * 专门针对中间表
	 * @param paras
	 * @param mUAllReq
	 *  表名 tmits
	 * @return
	 */
	private StringBuilder tradeStasticsSql(Map<String, Object> paras, MUAllReq mUAllReq,StringBuilder sbConditionOuter,boolean needJoin){
		StringBuilder sbTable=new StringBuilder();
		boolean f=isNotNeedQueryTradeStastics(mUAllReq);
		if (!f||needJoin) {
			sbTable.append(" LEFT JOIN t_money_investor_tradeorder_statistics tmits ")
			.append(" ON tmits.investorOid = mib.userOid ")
			;
			if (null != mUAllReq.getTotalInvestAmountStart()) {
				sbConditionOuter.append(" and tmits.totalInvestAmount>= :TOTALINVESTAMOUNTSTART");
				paras.put("TOTALINVESTAMOUNTSTART", mUAllReq.getTotalInvestAmountStart());
			}
			if (null != mUAllReq.getTotalInvestAmountEnd()) {
				sbConditionOuter.append(" and tmits.totalInvestAmount<= :TOTALINVESTAMOUNTEND   ");
				
				paras.put("TOTALINVESTAMOUNTEND", mUAllReq.getTotalInvestAmountEnd());
			}
			if (null != mUAllReq.getLastTradeTimeStart()) {
				sbConditionOuter.append("and tmits.lastTradeTime >= :LASTTRADETIMESTART ");
				paras.put("LASTTRADETIMESTART", mUAllReq.getLastTradeTimeStart());
			}
			if (null != mUAllReq.getLastTradeTimeEnd()) {
				sbConditionOuter.append("and tmits.lastTradeTime <= :LASTTRADETIMEEND ");
				paras.put("LASTTRADETIMEEND", mUAllReq.getLastTradeTimeEnd());
			}
		}
		return sbTable;
	}
	
	public static void main(String[] args) {
//		Integer a=Optional.of(4).filter(t->t.equals(4)).get();
//		System.out.println(a);
		boolean f=Stream.of(null," ").allMatch(t->{return null==t||Objects.equal(t, "");});
		System.out.println(f);
	}
	

}
