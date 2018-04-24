package com.guohuai.ams.cashtool.pool;

import java.util.List;

import org.springframework.data.domain.Page;

import com.guohuai.component.web.view.PageResp;

//import com.guohuai.asset.hill.component.web.view.PageResp;

public class CashToolRevenueListResp extends PageResp<CashToolRevenue> {

	public CashToolRevenueListResp() {
		super();
	}

	public CashToolRevenueListResp(Page<CashToolRevenue> page) {
		this(page.getContent(), page.getTotalElements());
	}

	public CashToolRevenueListResp(List<CashToolRevenue> list) {
		this(list, list.size());
	}

	public CashToolRevenueListResp(List<CashToolRevenue> list, long total) {
		this();
		super.setTotal(total);
		super.setRows(list);
	}

}
