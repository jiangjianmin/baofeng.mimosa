package com.guohuai.mmp.investor.baseaccount.refer.details;

import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestoRefErDetailsCountRep extends BaseRep {

	private String registNum;
	
	private String bindBankNum;
	
	private int friendInvestCount;// 好友投资笔数
	
	private List<InvestoRefErDetailsRep> recommendInfo;
	
	private List<InvestoRefErDetailsRep> bindBankInfo;
}
