package com.guohuai.mmp.investor.baseaccount.refer.details;

import java.util.Date;

import com.guohuai.component.util.StringUtil;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 我的推荐列表 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestoRefErDetailsRep {

	public InvestoRefErDetailsRep(InvestoRefErDetailsEntity entity) {
		this.phone = StringUtil.kickstarOnPhoneNum(entity.getInvestorBaseAccount().getPhoneNum());
		this.date = entity.getCreateTime();
		this.realName = StringUtil.kickstarOnRealname(entity.getInvestorBaseAccount().getRealName());
	}

	private String phone;
	private String realName;
	private Date date;

}
