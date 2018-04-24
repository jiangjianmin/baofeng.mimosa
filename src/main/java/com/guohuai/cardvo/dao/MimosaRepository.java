package com.guohuai.cardvo.dao;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;


/**
 * 更改mimosa锁定状态
 * @author yujianlong
 *
 */
public interface MimosaRepository extends JpaRepository<InvestorTradeOrderEntity, String>, JpaSpecificationExecutor<InvestorTradeOrderEntity>{
	/**
	 * 更改用户锁定状态
	 * @param cardlockstatus
	 * @param userOid
	 * @return
	 */
	@Modifying
	@Query(value="update t_money_investor_tradeorder a set a.cardlockstatus=?1 where a.investorOid=?2",nativeQuery=true)
	public int changeTradeorderUserLockStatus(int cardlockstatus,String userOid);
	@Modifying
	@Query(value="update t_money_investor_statistics a set a.cardlockstatus=?1 where a.investorOid=?2",nativeQuery=true)
	public int changeStasticsUserLockStatus(int cardlockstatus,String userOid);
	
	
	/**
	 * 查看用户是否绑卡过
	 * @param userOid
	 * @return
	 */
	@Query(value="select t1.idNum from t_money_investor_baseaccount t1 where t1.userOid = ?1 ",nativeQuery=true)
	public Object findIdNumByUserOid(String userOid);
	
	/**
	 * 更改baseaccount的锁定状态
	 * @param cardlockstatus
	 * @param userOid
	 * @return
	 */
	@Modifying
	@Query(value="update t_money_investor_baseaccount a set a.cardlockstatus=?1 where a.userOid=?2",nativeQuery=true)
	public int changeBaseAccountUserLockStatus(int cardlockstatus,String userOid);
	

	/**
	 * 定时插入末次交易数据
	 * @param orderTypeList
	 * @param orderStatusList
	 * @return
	 */
	@Modifying
	@Query(value=" insert into t_money_investor_lasttradeorder(investorOid,orderTime,productOid)  "
			+" select * from  "
			+" (select investorOid, "
			+" max(orderTime) as orderTime, "
			+" (select productOid from t_money_investor_tradeorder where investorOid=a.investorOid AND "
			+" orderTime=max(a.orderTime) limit 1 "
			+" ) as productOid "
			+" from t_money_investor_tradeorder a "
			+" where 1=1 "
			+" and a.orderType in(?1) "
			+" and a.orderStatus in(?2) "
			+" group by investorOid) t1 "
			+" on DUPLICATE KEY UPDATE orderTime=t1.orderTime,productOid=t1.productOid "
			,nativeQuery=true)
	public int insertLastTradeOrder(Collection<String> orderTypeList,Collection<String> orderStatusList);
	

	
}
