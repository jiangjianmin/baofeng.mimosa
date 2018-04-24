package com.guohuai.component.web.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PagesRep<T> extends BaseRep {

	List<T> rows = new ArrayList<T>();

	public void add(T e) {
		rows.add(e);
	}

	long total;
	
	long realTotal;
	
	BigDecimal totalAmount=BigDecimal.ZERO;
}
