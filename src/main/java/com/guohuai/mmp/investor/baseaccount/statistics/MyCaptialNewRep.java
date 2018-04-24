package com.guohuai.mmp.investor.baseaccount.statistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的资产（账户余额，活期总资产，定期总资产） */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MyCaptialNewRep extends BaseRep {

	/**
	 * 资产总额
	 */
	private BigDecimal totalCapitalAmount = BigDecimal.ZERO;

	private List<CapitalRow> capitalList = new ArrayList<>();
}
