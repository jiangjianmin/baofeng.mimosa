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
public class GuessItemRep extends BaseRep{
	
	List<String> content = new ArrayList<String>();//答案列表
	List<String> oids = new ArrayList<String>();//答案oid列表
	List<BigDecimal> percents = new ArrayList<BigDecimal>();//开奖结果加减息列表

}
