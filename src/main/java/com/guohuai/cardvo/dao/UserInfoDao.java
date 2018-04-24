package com.guohuai.cardvo.dao;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;
import com.guohuai.basic.cardvo.req.userInfoReq.MUAllReq;
import com.guohuai.cardvo.util.CardVoUtil;

/**
 * 用户信息查询dao 直接用原生jpa sql查询
 * 
 * @author huyong
 * @date 2017.5.25
 */
@Repository
public class UserInfoDao {

	/**
	 * 绑定jpa EntityManager
	 */
	@PersistenceContext
	private EntityManager em;

	
	/**
	 * @desc 根据用户oid查询用户明细
	 * @author huyong
	 * @date 2017.5.25
	 */
	public List<Map<String, Object>> getUserListByOids(MUAllReq mUAllReq){
		StringBuffer sb = new StringBuffer(80);
		sb.append(" select t1.userOid,t1.realName,t1.phoneNum,DATE_FORMAT(t1.createTime,'%Y-%m-%d %H:%i:%s') createTime from t_money_investor_baseaccount t1 where t1.userOid in (:userOidList)");
		Query emQuery=em.createNativeQuery(sb.toString());
		emQuery.setParameter("userOidList", mUAllReq.getIdList());
		sb = null;
		return CardVoUtil.query2Map(emQuery);
	}
	
	

}
