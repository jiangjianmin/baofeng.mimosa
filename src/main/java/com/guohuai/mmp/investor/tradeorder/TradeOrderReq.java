package com.guohuai.mmp.investor.tradeorder;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class TradeOrderReq {

	/**
	 * 所属理财产品
	 */
	@NotBlank(message = "所属理财产品OID不能为空 ")
	@Length(min = 32, max = 32, message = "所属理财产品OID有问题")
	String productOid;

	/**
	 * 申购金额
	 */
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	@NotNull(message = "金额不能为空")
	BigDecimal moneyVolume;

	
	String cid;
	
	String ckey;

	/** 卡券编号 */
	String couponId;

	/** 卡券类型 */
	String couponType;

	/** 卡券实际抵扣金额 */
	BigDecimal couponDeductibleAmount;

	/** 卡券金额 */
	BigDecimal couponAmount;

	/** 投资者实付金额 */
	BigDecimal payAmouont;
	String uid;
	
	/**竞猜宝选项id */
	String guessItemOid;
	String protocalOid;
	String orderOid;
	
	String orderType;
	
	BigDecimal ratio;
	Integer raiseDays;
	Integer cardId;
	Integer durationPeriodDays;
}
