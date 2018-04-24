package com.guohuai.mmp.platform.payment;

import java.io.Serializable;
import java.math.BigDecimal;

@lombok.Data
public class RedeemPayRequest implements Serializable {
	private static final long serialVersionUID = -112765746294580721L;
	private String userOid;
	private String orderNo;
	private String type;
	private BigDecimal amount = BigDecimal.ZERO;

	private BigDecimal fee = BigDecimal.ZERO;
	private String remark;
	private String describe;
	private String province;
	private String city;
	private String orderTime;
	private String payDate;
}