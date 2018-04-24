package com.guohuai.mmp.platform;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class StaticProperties {


	/** 协议存放目录 */
	@Value("${lecurrent.splitby}")
	String splitby;
	
	public static int stSplitby;
	
	static boolean is15;
	static boolean is24;
	/**
	 * 创建文件目录
	 */
	@PostConstruct
	public void init(){
		StaticProperties.stSplitby = Integer.parseInt(splitby);
		if (stSplitby == 0)  {
			is24 = true;
			is15 = false;
		} 
		if (stSplitby == 15)  {
			is24 = false;
			is15 = true;
		}
	}
	
	public static boolean isIs15() {
		return is15;
	}
	public static boolean isIs24() {
		return is24;
	}
	
}
