package com.guohuai.women.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtil {
	
	/** 
	  *method 将字符串类型的日期转换为一个timestamp（时间戳记java.sql.Timestamp） 
	   dateString 需要转换为timestamp的字符串 
	   dataTime timestamp 
	  */ 
	public final static java.sql.Timestamp string2Time(String dateString) { 
	  DateFormat dateFormat; 
	  dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设定格式 
	  dateFormat.setLenient(false); 
	  java.util.Date timeDate = null;
		try {
			timeDate = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	  java.sql.Timestamp dateTime = new java.sql.Timestamp(timeDate.getTime());//Timestamp类型,timeDate.getTime()返回一个long型 
	  return dateTime; 
	} 

}
