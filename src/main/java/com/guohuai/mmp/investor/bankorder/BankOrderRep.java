package com.guohuai.mmp.investor.bankorder;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class BankOrderRep extends BaseRep {
	private String retHtml;
	private String bankOrderOid;
}
