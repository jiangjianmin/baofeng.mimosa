package com.guohuai.ams.investment.pool;

import java.util.List;

import org.springframework.data.domain.Page;

import com.guohuai.component.web.view.PageResp;

//import com.guohuai.asset.hill.component.web.view.PageResp;

public class TargetIncomeListResp extends PageResp<TargetIncome> {

	public TargetIncomeListResp() {
		super();
	}

	public TargetIncomeListResp(Page<TargetIncome> page) {
		this(page.getContent(), page.getTotalElements());
	}

	public TargetIncomeListResp(List<TargetIncome> list) {
		this(list, null == list ? 0 : list.size());
	}

	public TargetIncomeListResp(List<TargetIncome> list, long total) {
		this();
		super.setTotal(total);
		super.setRows(list);
	}

}
