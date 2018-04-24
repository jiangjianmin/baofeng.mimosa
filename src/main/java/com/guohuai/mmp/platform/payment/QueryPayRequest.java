package com.guohuai.mmp.platform.payment;

import java.io.Serializable;

@lombok.Data
public class QueryPayRequest implements Serializable {
	private static final long serialVersionUID = -112765746294580721L;
	private String userOid;
	private String orderNo;

}