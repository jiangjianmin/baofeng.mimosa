package com.guohuai.mmp.publisher.baseaccount.loginacc;

import java.util.List;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AllAccRep extends BaseRep {
	List<String> allAcc;
	List<String> selectedAcc;
}
