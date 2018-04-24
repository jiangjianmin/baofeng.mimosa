package com.guohuai.ams.guess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryReq extends BaseRep {
	
	String guessOid;//活动oid
	
	List<BigDecimal> percents = new ArrayList<BigDecimal>();//利息百分比增加/减少列表
	List<String> oids = new ArrayList<String>();//答案oid列表

}
