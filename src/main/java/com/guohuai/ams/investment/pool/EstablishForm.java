/*   
 * Copyright © 2015 guohuaigroup All Rights Reserved.   
 *   
 * This software is the confidential and proprietary information of   
 * Founder. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Founder.   
 *   
 */
package com.guohuai.ams.investment.pool;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * <p>
 * Title: EstablishForm.java
 * </p>
 * <p>
 * 标的成立表单
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年5月18日 下午7:37:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstablishForm implements Serializable {

	/**
	 * @Fields serialVersionUID :
	 */

	private static final long serialVersionUID = 2246250729662434487L;
	@NotNull(message = "投资标的id不能为空")
	private String oid;

	private String risk;

	@NotNull(message = "投资标的成立日期不能为空")
	private Date setDate;
	@NotNull(message = "收益起始日不能为空")
	private Date incomeStartDate;
	@NotNull(message = "收益截止日不能为空")
	private Date incomeEndDate;

//	@NotNull(message = "投资标的首付息日不能为空")
//	private Date arorFirstDate;

	/**
	 * 付息周期方式
	 */
	// @NotNull(message = "投资标的付息周期方式不能为空")
	// private String accrualCycleType;
	

	/**
	 * 合同年天数
	 */
//	@NotNull(message = "合同年天数不能为空")
//	private Integer contractDays;

	/**
	 * 付息日
	 */
	@NotNull(message = "投资标的付息日不能为空")
	private Integer accrualDate;
	/**
	 * 募集期收益
	 */
//	@NotNull(message = "投资标的募集期收益不能为空")
//	private BigDecimal collectIncomeRate;

	// @NotNull(message = "投资标的收益截止日期不能为空")
	// private Timestamp arorFirstDate;

	/**
	 * 操作员
	 */
	private String operator;
}
