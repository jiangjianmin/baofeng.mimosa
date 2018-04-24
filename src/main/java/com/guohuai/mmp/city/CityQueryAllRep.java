package com.guohuai.mmp.city;

import java.util.List;

import lombok.EqualsAndHashCode;
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
public class CityQueryAllRep {

	String value;
	
	String text;
	
	List<CityQueryRep> children;
}
