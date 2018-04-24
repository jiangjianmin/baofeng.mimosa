/*   
 * Copyright © 2015 guohuaigroup All Rights Reserved.   
 *   
 * This software is the confidential and proprietary information of   
 * Founder. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Founder.   
 *   
 */
package com.guohuai.ams.cashtool.pool;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.cashtool.CashTool;

public interface CashtoolRevenueDao extends JpaRepository<CashToolRevenue, String>, JpaSpecificationExecutor<CashToolRevenue> {

	/**
	 * 根据现金管理工具id删除某一天的收益
	 * 
	 * @Title: deleteByDailyProfitDate
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param cashtoolOid
	 * @param dailyProfitDate
	 */
	@Modifying
	@Query("delete from CashToolRevenue cr where cr.cashTool.oid=?1 and cr.dailyProfitDate=?2")
	public void deleteByDailyProfitDate(String cashtoolOid, Date dailyProfitDate);

	/**
	 * 根据现金管理工具id和交易日期查询收益
	 * 
	 * @Title: findCashtoolRevenue
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param cashtoolOid
	 * @param dailyProfitDate
	 * @return CashToolRevenue 返回类型
	 */
	@Query("from CashToolRevenue cr where cr.cashTool.oid=?1 and cr.dailyProfitDate=?2")
	public CashToolRevenue findCashtoolRevenue(String cashtoolOid, Date dailyProfitDate);

	/**
	 * 根据现金管理工具id集合和交易日期查询收益
	 * 
	 * @Title: findCashtoolRevenue
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param cashtoolOids
	 * @param dailyProfitDate
	 * @return List<CashToolRevenue> 返回类型
	 */
	@Query("from CashToolRevenue cr where cr.cashTool.oid in ?1 and cr.dailyProfitDate=?2")
	public List<CashToolRevenue> findCashtoolRevenue(List<String> cashtoolOids, Date dailyProfitDate);

	public List<CashToolRevenue> findByCashToolAndDailyProfitDate(CashTool cashTool, Date dailyProfitDate);
}
