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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @ClassName: CashToolRevenue
 * @Description: 现金管理工具 收益率实体
 * @author vania
 * @date 2016年5月16日 上午10:15:56
 *
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class CashToolRevenueForm implements Cloneable, Serializable {

	/**
	 * @Fields serialVersionUID : TODO
	 */

	private static final long serialVersionUID = -2361258913434461386L;

	/**
	 * 关联现金管理工具
	 */
	@NotNull(message = "关联现金管理工具id不能为空")
	private String cashtoolOid;

	/**
	 * 交易日期
	 */
	@NotNull(message = "交易日期不能为空")
	private Date dailyProfitDate;

	/**
	 * 万份收益
	 */
	@NotNull(message = "万份收益不能为空")
	@Digits(integer = 4, fraction = 4, message = "万份收益最大4位整数4位小数")
	private BigDecimal dailyProfit;
	

	// @NotNull(message = "投资标的收益截止日期不能为空")
	// private Timestamp arorFirstDate;
	
	
	/**
	 * 7日年化收益率
	 */
	@NotNull(message = "7日年化收益率不能为空")
	@Digits(integer = 4, fraction = 4, message = "7日年化收益率最大4位整数4位小数")
	private BigDecimal weeklyYield;

	/**
	 * 操作员
	 */
	@Size(max = 32, message = "操作人最大32个字符！")
	private String operator;

	/**
	 * 创建时间
	 */
	private Timestamp createTime;

	/**
	 * 修改时间
	 */
	private Timestamp updateTime;

}
