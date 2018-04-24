package com.guohuai.mmp.platform.publisher.offset;

import java.math.BigDecimal;

import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class VolumeConfirmRep {
	BigDecimal investAmount = SysConstant.BIGDECIMAL_defaultValue, redeemAmount = SysConstant.BIGDECIMAL_defaultValue;
	boolean isSuccess = true;
}
