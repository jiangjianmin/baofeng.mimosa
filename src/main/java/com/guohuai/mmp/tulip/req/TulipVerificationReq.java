package com.guohuai.mmp.tulip.req;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 卡券核销请求参数 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TulipVerificationReq implements Serializable {

	private static final long serialVersionUID = 5706029906665535170L;

	/** 订单流水号 */
	private String orderCode;

	/** 订单金额 */
	private BigDecimal orderAmount;

	/** 投资者实付金额 */
	private BigDecimal payAmount;

	/** 卡券编号 */
	private String couponId;

	/** 卡券金额/卡券抵扣金额 */
	private BigDecimal couponAmount;

	/** 产品编号(oid) */
	private String proOid;

	/** 产品名称 */
	private String productName;

}
