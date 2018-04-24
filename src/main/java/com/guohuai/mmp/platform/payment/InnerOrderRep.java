package com.guohuai.mmp.platform.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InnerOrderRep {
	

	private String orderNo;
	private String returnCode;

}
