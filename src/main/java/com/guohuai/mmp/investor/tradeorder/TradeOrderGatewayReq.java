package com.guohuai.mmp.investor.tradeorder;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@lombok.Data
@NoArgsConstructor
@ToString(callSuper = true)
public class TradeOrderGatewayReq extends TradeOrderReq{
	String bankCode;
	String paymentMode;
}
