package com.guohuai.ams.duration.fact.income;

import com.guohuai.component.web.view.BaseResp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class IncomeAllocateBaseResp extends BaseResp {

	public String oid;
	
}
