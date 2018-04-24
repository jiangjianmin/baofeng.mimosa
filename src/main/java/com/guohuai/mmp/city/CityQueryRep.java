package com.guohuai.mmp.city;

import lombok.EqualsAndHashCode;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
public class CityQueryRep {

	String value, text;
	
}
