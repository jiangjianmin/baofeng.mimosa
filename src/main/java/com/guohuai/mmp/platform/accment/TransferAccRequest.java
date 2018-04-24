package com.guohuai.mmp.platform.accment;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TransferAccRequest {
	/**
	 * 入账账户ID	inputAccountNo	
出账账户ID	outpuptAccountNo	
订单金额	balance	
请求流水号	requestNo	
单据类型	orderType	基本户到发行人账户，备付金到超级户，备付金到基本户
交易用途	remark	
定单号	orderNo	

	 */
	private String inputAccountNo;
	
	
	private String outpuptAccountNo;
	
	private BigDecimal balance;
	
	private String orderType;
	
	private String remark;
	
	private String orderNo;
}
