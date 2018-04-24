package com.guohuai.mmp.platform.publisher.offset;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class OffsetMoneyReq {
	List<Money> offsetMoneyList = new ArrayList<Money>();
	String offsetOid;
	BigDecimal money;
	BigDecimal fee;
	BigDecimal lexinFee;
	String type;  //offsetPay or offsetCollect or payPlatformFee
}
