package com.guohuai.mmp.investor.baseaccount;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvestorBaseAccountIsLoginRep extends BaseRep {

	boolean islogin;
}
