package com.guohuai.mmp.platform.publisher.offset;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@lombok.Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {
	String channelOid; //渠道oid
	String busDate;  //业务发生时间
	String productOid; //产品id
	String type;  //offsetPay or offsetCollect or payPlatformFee
	BigDecimal money;   //金额
	BigDecimal couFee;   //手续费
	BigDecimal lexinFee;  //乐信费
	BigDecimal lexinCouFee;  //支付乐信费用的手续费
	String accountInfo; //spv卡号
	String accountType;  //spv账户类型
	String combinePay;   //是否合并支付
	BigDecimal actualAmount; //实记金额
	String customerId;     //关联方id
	String customerInfo;   //关联方账户
	String couCustomerId;     //手续费关联方id
	String couCustomerInfo;   //手续费关联方账户
}
