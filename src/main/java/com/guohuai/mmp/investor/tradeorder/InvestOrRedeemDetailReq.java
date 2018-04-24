package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class InvestOrRedeemDetailReq {
	
	String gcbatch;//轧差批次
	
	String orderType;//订单类型

	/** 订单状态 */
	String orderStatus;

	/** 最小金额 */
	String minOrderAmount;

	/**最大金额 **/
	String maxOrderAmount;

	String beginTime;
	
	String endTime;
	
	/** 手机号 */
	String phoneNum;

	/** 用户姓名 */
	String realName;
	
   /**用户身份证号**/
	String idCardNo;
	
	/**结算银行账号**/ 
	String bankNo;
	
	private int page = 1;
	private int rows = 10;
}
