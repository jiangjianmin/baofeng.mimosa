package com.guohuai.bfsms;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class BfSMSVeriCodeRep extends BaseRep {

	String veriCode;
}
