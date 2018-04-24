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
import java.sql.Timestamp;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * <p>Title: EstablishForm.java</p>    
 * <p>标的成立表单 </p>   
 * @author vania      
 * @version 1.0    
 * @created 2016年5月18日 下午7:37:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetOverdueForm implements Serializable {

	
	/**
	 * @Fields serialVersionUID : TODO
	 */
	
	private static final long serialVersionUID = -5951077006998356229L;

	@NotNull(message = "投资标的id不能为空")
	private String oid;
	@NotNull(message = "逾期日期不能为空")
	private Date overdueDate;
	@NotNull(message = "逾期天数不能为空")
	private Integer overdueDays;
	@NotNull(message = "逾期利率不能为空")
	@Digits(integer = 4, fraction = 4, message = "逾期利率最大4位整数4位小数")
	private BigDecimal overdueRate;
//	@NotNull(message = "滞纳金不能为空")
	@Digits(integer = 10, fraction = 4, message = "滞纳金最大10位整数4位小数")
	private BigDecimal overdueFine;

	// @NotNull(message = "投资标的收益截止日期不能为空")
	// private Timestamp arorFirstDate;

	/**
	 * 操作员
	 */
	@Size(max = 32, message = "操作人最大32个字符！")
	private String operator;
	private Timestamp createTime;
}
