package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MaxHoldVolRep extends BaseRep {
	BigDecimal maxHoldVol;
}
