package com.guohuai.mmp.investor.baseaccount;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestorBaseAccountIsRegistRep extends BaseRep {

	private boolean regist;
	
	private String accountOid;
}
