package com.guohuai.ams.supplement.order;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class RestOrderAmountRep  extends BaseRep{
	
	BigDecimal restOrderAmount;

}
