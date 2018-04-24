package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class InterestRep {
	BigDecimal amount = SysConstant.BIGDECIMAL_defaultValue;
	BigDecimal rewardAmount = SysConstant.BIGDECIMAL_defaultValue;
	BigDecimal baseAmount = SysConstant.BIGDECIMAL_defaultValue;
	boolean result = true;
}
