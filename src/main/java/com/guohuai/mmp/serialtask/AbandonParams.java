package com.guohuai.mmp.serialtask;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AbandonParams {
	private String orderCode;

	private BigDecimal orderAmount;
}
