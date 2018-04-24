package com.guohuai.ams.duration.fact.income;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 收益发放返回实体设计
 * @author wangyan
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocateIncomeReturn implements Serializable {

	private static final long serialVersionUID = 3731352284709737463L;
	
	/**
	 * 分配状态  (ALLOCATED: 发放完成;  ALLOCATEFAIL: 发放失败) 
	 */
	public static final String STATUS_Allocated = "ALLOCATED";
	public static final String STATUS_AllocateFail = "ALLOCATEFAIL";
	
	private String productOid; // 产品oid
	private Date  allocateDate; // 收益日(最多是T-1日)
	private BigDecimal successAllocateIncome; // 成功分配收益金额
	private BigDecimal leftAllocateIncome; // 未分配金额
	private int successAllocateInvestors; // 成功分配投资者数
	private int failAllocateInvestors; // 失败分配投资者数
	private String status; // 分配状态 
		

}
