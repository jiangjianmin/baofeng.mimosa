package com.guohuai.mmp.tulip.rep;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MyRateCouponRep extends BaseRep {

	/** 加息金额 */
	private BigDecimal rateAmount = SysConstant.BIGDECIMAL_defaultValue;

}
