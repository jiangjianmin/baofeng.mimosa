package com.guohuai.ams.supplement.order;


import java.util.ArrayList;
import java.util.List;

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
public class TnProductRep extends BaseRep{
	
	List<TnProduct> tns = new ArrayList<>();
	
}
