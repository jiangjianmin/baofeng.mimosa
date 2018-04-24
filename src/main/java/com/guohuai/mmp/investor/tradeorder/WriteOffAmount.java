package com.guohuai.mmp.investor.tradeorder;

import java.io.Serializable;
import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class WriteOffAmount extends BaseRep  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9177764489111962580L;
	BigDecimal writeOffAmount;
}
