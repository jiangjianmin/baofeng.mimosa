package com.guohuai.mmp.investor.bank;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class BankAgreeRep extends BaseRep {

	/** 请求流水号 */
	private String reuqestNo;
	
	/** 订单号 */
	private String OrderNo;
}
