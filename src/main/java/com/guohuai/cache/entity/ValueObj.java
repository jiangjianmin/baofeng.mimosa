package com.guohuai.cache.entity;

import java.io.Serializable;

public class ValueObj implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8283876997003473257L;
	
	public ValueObj() {
		super();
	}
	public ValueObj(Object value, Object backValue) {
		super();
		this.value = value;
		this.backValue = backValue;
	}
	Object value;
	Object backValue;
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public Object getBackValue() {
		return backValue;
	}
	public void setBackValue(Object backValue) {
		this.backValue = backValue;
	}
	
	
}
