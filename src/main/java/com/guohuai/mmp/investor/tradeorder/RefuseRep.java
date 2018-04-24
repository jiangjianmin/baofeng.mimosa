package com.guohuai.mmp.investor.tradeorder;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@lombok.Builder
@AllArgsConstructor
public class RefuseRep extends BaseRep {
	boolean success = true;
	
}
