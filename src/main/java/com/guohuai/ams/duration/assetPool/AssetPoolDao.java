package com.guohuai.ams.duration.assetPool;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AssetPoolDao extends JpaRepository<AssetPoolEntity, String>, JpaSpecificationExecutor<AssetPoolEntity> {

	// 获取所有已成立的资产池id和名称列表
	@Query(value = "SELECT a.oid, a.name FROM T_GAM_ASSETPOOL a WHERE a.state = 'ASSETPOOLSTATE_02' order by a.createTime desc", nativeQuery = true)
	public List<Object> findAllNameList();
	
	@Query("from AssetPoolEntity a where a.name like ?1")
	public List<AssetPoolEntity> getListByName(String name);
	
	@Query(value = "SELECT * FROM T_GAM_ASSETPOOL a WHERE a.state = 'ASSETPOOLSTATE_02' order by a.createTime desc LIMIT 1", nativeQuery = true)
	public AssetPoolEntity getLimitOne();
	
	@Query("from AssetPoolEntity a where a.state = 'ASSETPOOLSTATE_02'")
	public List<AssetPoolEntity> getListByState();
	
	@Query(value = "update T_GAM_ASSETPOOL set state = 'ASSETPOOLSTATE_04' where oid = ?1", nativeQuery = true)
	@Modifying
	public void updateAssetPool(String pid);
	
	@Query(value = "update AssetPoolEntity set scale = scale - ?2 where oid = ?1")
	@Modifying
	public void subScale(String pid, BigDecimal volume);
	
	@Query(value = " SELECT COUNT(1) FROM t_gam_assetpool WHERE baseAssetCode = ?1 ", nativeQuery = true)
	public int queryBaseAssetCode(String baseAssetCode);
	// 根据资产池名称、产品名称、产品状态、开始募集时间、还本付息时间查询 资产池-产品 信息
	@Query(value = "SELECT b.oid, b.code, b.name AS productName, b.type, b.durationPeriodDays, b.collectedVolume, a.name AS assetPoolName,"
			+ " (SELECT sum(c.orderAmount) FROM t_money_investor_tradeorder c WHERE c.productOid = b.oid AND c.orderType = 'noPayInvest' AND c.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')) AS noPayInvest,"
			+ " (SELECT sum(d.orderAmount) FROM t_money_investor_tradeorder d WHERE d.productOid = b.oid AND d.orderType = 'invest' AND d.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')) AS payInvest,"
			+ " b.raiseStartDate, b.repayDate, b.repayLoanStatus, b.repayInterestStatus,"
			+ " if(b.state = 'CLEARED', (SELECT sum(e.orderAmount) FROM t_money_investor_tradeorder e WHERE e.productOid = b.oid AND e.orderType IN ('cash', 'cashFailed') AND e.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')), NULL) AS cash,"
			+ " if(b.state = 'CLEARED', (SELECT sum(f.orderAmount) FROM t_money_investor_tradeorder f WHERE f.productOid = b.oid AND f.orderType IN ('invest', 'noPayInvest') AND f.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')), NULL) AS invest,"
			+ " if(b.state = 'CLEARED', (SELECT sum(g.totalIncome) FROM t_money_investor_tradeorder g WHERE g.productOid = b.oid AND g.orderType IN ('invest', 'noPayInvest') AND g.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')), NULL) AS income,"
			+ " b.state,b.createTime,b.operator"
			+ " FROM t_gam_assetpool a LEFT JOIN t_gam_product b ON a.oid = b.assetPoolOid"
			+ " WHERE b.type = 'PRODUCTTYPE_01' AND a.name LIKE ?1 AND b.name LIKE ?2 AND b.state = if(?3 = '', b.state, ?3)"
			+ " AND b.raiseStartDate BETWEEN if(?4 = '', date('2000-01-01'), ?4) AND if(?5 = '', date('2999-12-12'), ?5)"
			+ " AND b.repayDate BETWEEN if(?6 = '', date('2000-01-01'), ?6) AND if(?7 = '', date('2999-12-12'), ?7)"
			+ " ORDER BY b.createTime DESC"
			+ " LIMIT ?8,?9", nativeQuery = true)
	public Object[] findAssetPoolAndProduct(String assetPoolName, String productName, String productStatus, String raiseTimeBegin, String raiseTimeEnd, String repayTimeBegin, String repayTimeEnd, int start, int size);
	
	// 根据资产池名称、产品名称、产品状态、开始募集时间、还本付息时间查询 资产池-产品 信息
	@Query(value = "SELECT count(1) as qty,sum(t.collectedVolume) as collectedVolume,sum(t.noPayInvest) as noPayInvest,sum(t.payInvest) as payInvest,sum(t.cash) as cash,sum(t.invest) as invest,sum(t.income) as income FROM"
			+ " (SELECT b.collectedVolume, "
			+ " (SELECT sum(c.orderAmount) FROM t_money_investor_tradeorder c WHERE c.productOid = b.oid AND c.orderType = 'noPayInvest' AND c.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')) AS noPayInvest,"
			+ " (SELECT sum(d.orderAmount) FROM t_money_investor_tradeorder d WHERE d.productOid = b.oid AND d.orderType = 'invest' AND d.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')) AS payInvest,"
			+ " if(b.state = 'CLEARED', (SELECT sum(e.orderAmount) FROM t_money_investor_tradeorder e WHERE e.productOid = b.oid AND e.orderType IN ('cash', 'cashFailed') AND e.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')), 0) AS cash,"
			+ " if(b.state = 'CLEARED', (SELECT sum(f.orderAmount) FROM t_money_investor_tradeorder f WHERE f.productOid = b.oid AND f.orderType IN ('invest', 'noPayInvest') AND f.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')), 0) AS invest,"
			+ " if(b.state = 'CLEARED', (SELECT sum(g.totalIncome) FROM t_money_investor_tradeorder g WHERE g.productOid = b.oid AND g.orderType IN ('invest', 'noPayInvest') AND g.orderStatus IN ('paySuccess', 'accepted', 'confirmed', 'done')), 0) AS income"
			+ " FROM t_gam_assetpool a LEFT JOIN t_gam_product b ON a.oid = b.assetPoolOid"
			+ " WHERE b.type = 'PRODUCTTYPE_01' AND a.name LIKE ?1 AND b.name LIKE ?2 AND b.state = if(?3 = '', b.state, ?3)"
			+ " AND b.raiseStartDate BETWEEN if(?4 = '', date('2000-01-01'), ?4) AND if(?5 = '', date('2999-12-12'), ?5)"
			+ " AND b.repayDate BETWEEN if(?6 = '', date('2000-01-01'), ?6) AND if(?7 = '', date('2999-12-12'), ?7)) t", nativeQuery = true)
	public Object[] findAssetPoolAndProduct(String assetPoolName, String productName, String productStatus, String raiseTimeBegin, String raiseTimeEnd, String repayTimeBegin, String repayTimeEnd);
	
	// 根据产品id列表 导出所有订单
	@Query(value="SELECT gp.fullName, mit.orderCode, mib.phoneNum, TRUNCATE(mit.orderAmount, 2) as orderAmount, TRUNCATE(mit.orderAmount, 2) as investAmount, TRUNCATE(mit.incomeAmount, 2) as incomeAmount,"
			+ " TRUNCATE(mit.orderAmount, 2) + TRUNCATE(mit.incomeAmount, 2) as cashAmount, mit.updateTime, gp.code, gp.operator"
			+ " FROM t_gam_product gp LEFT JOIN t_money_investor_tradeorder mit ON gp.oid = mit.productOid"
			+ " LEFT JOIN t_money_investor_baseaccount mib ON mit.investorOid = mib.oid"
			+ " WHERE gp.oid IN ?1 AND mit.orderType IN ('invest', 'noPayInvest') AND mit.orderStatus = 'confirmed';", nativeQuery=true)
	public List<Object[]> findAssetProductOrderList(List<String> oidCollection);

	/**
	 *
	 *获取04产品所有募集份额总和
	 * @author yujianlong
	 * @date 2018/4/1 18:52
	 * @param []
	 * @return java.math.BigDecimal
	 */
	@Query(value = "select ifnull(sum(t2.collectedVolume),0) from t_gam_assetpool t1,t_gam_product t2 where t1.oid = t2.assetPoolOid and t2.type = 'PRODUCTTYPE_04' ", nativeQuery = true)
	public BigDecimal getAssetPoolVolume();
}
