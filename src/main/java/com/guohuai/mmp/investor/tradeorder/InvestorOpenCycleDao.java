package com.guohuai.mmp.investor.tradeorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 *
 *循环开放产品订单关系dao
 * @author yujianlong
 * @date 2018/3/23 16:24
 * @param
 * @return
 */
public interface InvestorOpenCycleDao extends JpaRepository<InvestorOpenCycleRelationEntity, String>, JpaSpecificationExecutor<InvestorOpenCycleRelationEntity>{
	/**
	 *
	 *获取中间表
	 * @author yujianlong
	 * @date 2018/4/2 01:44
	 * @param [orderCodes]
	 * @return java.util.List<com.guohuai.mmp.investor.tradeorder.InvestorOpenCycleRelationEntity>
	 */
	@Query(value = "select * from  T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION "
			+ " where sourceOrderCode in(?1)  ",nativeQuery = true)
	public List<InvestorOpenCycleRelationEntity> getInvestorOpenCycleRelationEntityIn(List<String> orderCodes);

	/**
	 * 设置续投状态
	 * @param uid
	 * @param orderCode
	 * @param status
	 * @return
	 */
	@Query(value = "UPDATE T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION SET continueStatus = ?3 WHERE investorOid = ?1 AND sourceOrderCode = ?2", nativeQuery = true)
	@Modifying
	int setContinueStatusByUidAndOrderCode(String uid, String orderCode, int status);

	/**
	 * @Desc: 根据申购单查询
	 * @author huyong
	 * @date 2018/4/10 下午4:15
	 */
	@Query(value = "select * from  T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION "
			+ " where investOrderCode = ?1 group by investOrderCode",nativeQuery = true)
	public InvestorOpenCycleRelationEntity getOrderByInvestOrderCode(String investOrderCode);

	/**
	 * @Desc: 根据赎回单查询
	 * @author huyong
	 * @date 2018/4/10 下午4:15
	 */
	@Query(value = "select * from  T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION "
			+ " where redeemOrderCode = ?1",nativeQuery = true)
	public InvestorOpenCycleRelationEntity getOrderByRedeemOrderCode(String redeemOrderCode);


	/**
	 *
	 *更新中间表手机号
	 * @author yujianlong
	 * @date 2018/4/17 17:01
	 * @param [userOid, oldPhone, newPhone]
	 * @return int
	 */
	@Query(value="UPDATE T_MONEY_INVESTOR_OPENCYCLE_TRADEORDER_RELATION SET phone = ?3 WHERE investorOid = ?1 AND phone = ?2", nativeQuery = true)
	@Modifying
	public int updateOpencycleRelationPhonenum(String userOid, String oldPhone, String newPhone);

}