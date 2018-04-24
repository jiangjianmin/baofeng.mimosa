package com.guohuai.mmp.publisher.investor.holdincome;

import java.math.BigDecimal;
import java.sql.Date;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.RowsRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的收益 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class IncomeRep extends BaseRep {

	/** 总收益 */
	private BigDecimal totalIncome = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 收益确认日期
	 */
	private Date confirmDate;

	/** 收益明细（第一页） */
	private RowsRep<MyInvestorIncomeOfDateRep> details;
	
	/**
	 * 收益总行数
	 */
	private int total = 0;
}
