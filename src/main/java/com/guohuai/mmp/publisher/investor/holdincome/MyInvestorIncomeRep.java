package com.guohuai.mmp.publisher.investor.holdincome;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MyInvestorIncomeRep extends BaseRep {

	/** 总收益 */
	private BigDecimal totalIncome = SysConstant.BIGDECIMAL_defaultValue;

	/** 收益明细（第一页） */
	private PagesRep<InvestorIncomeRep> details;

}
