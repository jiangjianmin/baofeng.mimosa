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
 * <p>Title: InterestForm.java</p>    
 * <p>Description: 描述 </p>   
 * @author vania      
 * @version 1.0    
 * @created 2016年5月19日 下午4:28:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetIncomeForm implements Cloneable, Serializable {

	/**
	 * @Fields serialVersionUID : TODO
	 */

	private static final long serialVersionUID = -5836436376933754921L;
	@NotNull(message = "投资标的id不能为空")
	private String targetOid;

	/**
	 * 兑付期数
	 */
	@NotNull(message = "兑付期数不能为空")
	private Integer seq;

	/**
	 * 实际收益
	 */
	@NotNull(message = "实际收益不能为空")
	@Digits(integer = 10, fraction = 4, message = "实际收益最大10位整数4位小数")
	private BigDecimal incomeRate;

	/**
	 * 收益支付日
	 */
	@NotNull(message = "收益支付日不能为空")
	private Date incomeDate;

	/**
	 * 操作员
	 */
	@Size(max = 32, message = "创建人最大32个字符！")
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
