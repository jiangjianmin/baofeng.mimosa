package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.component.web.view.BaseRep;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TradeOrderGatewayRep extends BaseRep {
	
	private String tradeOrderOid;
	private String orderStatus;
	private String gatewayForm;

	
}
