package com.guohuai.mmp.platform.accment;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class AccountQueryIRep extends BaseRep {
	private String userOid;
	
}
