package com.guohuai.ams.investment;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InvestmentDao extends JpaRepository<Investment, String>, JpaSpecificationExecutor<Investment> {
	/**
	 * 根据名称模糊查询投资标的
	 * 
	 * @Title: getCashToolByName
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param name
	 * @return List<Object> 返回类型
	 */
	@Query("select ct.oid, ct.name from Investment ct where ct.name like ?1")
	public List<Object> getInvestmentByName(String name);
	
	/**
	 * 根据生命状态查询投资标的
	 * 
	 * @Title: findByLifeState
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param name
	 * @return List<Investment> 返回类型
	 */
	public List<Investment> findByLifeState(String lifeState);

	/**
	 * 增加持仓金额
	 * 
	 * @Title: incHoldAmount
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param oid
	 * @param holdAmount
	 * @return Investment 返回类型
	 */
	@Modifying
	@Query("update Investment set holdAmount = holdAmount + ?2 where oid = ?1")
	public int incHoldAmount(String oid, BigDecimal holdAmount);

	/**
	 * 增加申请金额
	 * 
	 * @Title: incApplyAmount
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param oid
	 * @param applyAmount
	 * @return Investment 返回类型
	 */
	@Modifying
	@Query("update Investment set applyAmount = applyAmount + ?2 where oid = ?1")
	public int incApplyAmount(String oid, BigDecimal applyAmount);
	
	/**
	 * 增加剩余授信额度
	 * 
	 * @Title: incRestTrustAmount
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param oid
	 * @param restTrustAmount
	 * @return Investment 返回类型
	 */
	@Modifying
	@Query("update Investment set restTrustAmount = restTrustAmount + ?2 where oid = ?1")
	public int incRestTrustAmount(String oid, BigDecimal restTrustAmount);
}
