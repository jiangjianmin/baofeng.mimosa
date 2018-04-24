package com.guohuai.moonBox.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.guohuai.component.util.Clock;

public class DateUtil {
	
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	static DateFormat  dateDateFormat = new SimpleDateFormat("yyyy-MM-dd");  
	/**
	 * 获取年分 如：2017
	 **/
  public static String GetDateYear(){
	  Calendar now = Calendar.getInstance();  
	  return String.valueOf(now.get(Calendar.YEAR));
  }
  /**
	 * 获取月份 如：06
	 **/
  public static String GetDateMonth(){
	  Calendar now = Calendar.getInstance();  
	  return String.valueOf(now.get(Calendar.MONTH) + 1);
  }
  /**
	 * 获取日 如：07
	 **/
  public static String GetDateDay(){
	  Calendar now = Calendar.getInstance();  
	  return String.valueOf(now.get(Calendar.DAY_OF_MONTH));
  }
  /**
 	 * 获取小时数 如：11
 	 **/
   public static String GetDateHour(){
 	  Calendar now = Calendar.getInstance();  
 	  return String.valueOf(now.get(Calendar.HOUR_OF_DAY));
   }
   /**
	 * 获取分钟数 如：42
	 **/
  public static  String GetDateMinute(){
	  Calendar now = Calendar.getInstance();  
	  return String.valueOf(now.get(Calendar.MINUTE));
  }
  /**
	 * 获取秒数 如：11
	 **/
  public  static String GetDateSecond(){
	  Calendar now = Calendar.getInstance();  
	  return String.valueOf(now.get(Calendar.SECOND));
  }
  /**
	 * 获取月末时间 如：2017-06-30
	 **/
  public  static String getMaxMonthDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return dateFormat.format(calendar.getTime());
	}
  /**
	 * 获取月初时间份 如：2017-06-01
	 **/
  public  static String getMinMonthDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		return dateFormat.format(calendar.getTime());
	}
  /**
	 * 获取系统当前SQL类型的Date
	 * 
	 * @return 当前时间
	 */
	public static   Date getSqlDate() {
		return new java.sql.Date(Clock.DEFAULT.getCurrentTimeInMillis());
	}
  public static Boolean CheckTime(String goalTime){
	  System.out.println("familyAutoInvestDate:"+goalTime);
//	  Date dates = getSqlDate();  
//      String nowTime=timeFormat.format(dates);
	  String nowTime=GetDateHour()+":"+GetDateMinute()+":"+GetDateSecond();
      System.out.println(nowTime);
      Calendar  c1=Calendar.getInstance();
      Calendar  c2=Calendar.getInstance();
      try {
			c1.setTime(timeFormat.parse(nowTime));//系统时间
			c2.setTime(timeFormat.parse(goalTime));
		} catch (ParseException e) {
			System.err.println("格式不正确");
			e.printStackTrace();
			return false;
		}
      if(c1.before(c2)||c1.equals(c2)){
    	  System.out.println("系统时间小于"+getSqlDate()+" "+goalTime+"点,当前时间为:"+nowTime);
    	  return true;
      }else if(c1.after(c2)){
    	  System.out.println("系统时间大于"+getSqlDate()+" "+goalTime+"点,当前时间为:"+nowTime);
    	  return false;
      }
      return true;
  }
  public static Boolean CheckDate(String goalDate){
	  Date dates = getSqlDate(); 
	  DateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd");  
      String nowTime=dateFormat.format(dates);
      System.out.println(nowTime);
      Calendar  c1=Calendar.getInstance();
      Calendar  c2=Calendar.getInstance();
      try {
			c1.setTime(sdf.parse(nowTime));//系统时间
			c2.setTime(sdf.parse(goalDate));
		} catch (ParseException e) {
			System.err.println("格式不正确");
			e.printStackTrace();
			return false;
		}
      int result=c1.compareTo(c2);
      if(result<=0){
  		System.out.println("系统时间小于等于"+goalDate+"点,当前时间为:"+nowTime);
  		return true;
  	  }else{
  		  System.out.println("系统时间大于"+goalDate+"点,当前时间为:"+nowTime);
  		  return false;
  	  }
  }
  public static Boolean Dateequals(String goalDate,int day){
	  Date dates = getSqlDate();   
	  DateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd");  
      String nowTime=dateFormat.format(dates);
      Calendar  c1=Calendar.getInstance();
      Calendar  c2=Calendar.getInstance();
      try {
			c1.setTime(sdf.parse(nowTime));//系统时间
			c1.add(Calendar.DATE,day);
			c2.setTime(sdf.parse(goalDate));
		} catch (ParseException e) {
			System.err.println("格式不正确");
			e.printStackTrace();
			return false;
		}
      int result=c1.compareTo(c2);
      if(result==0){
  		System.out.println("系统时间等于"+goalDate+"点,当前时间为:"+nowTime);
  		return true;
  	  }else{
  		  System.out.println("系统时间不等于"+goalDate+"点,当前时间为:"+nowTime);
  		  return false;
  	  }
  }
  /**
	 * 获取下一日时间
	 * 
	 * @return
	 */
	public static  Date getafterDate() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 1);
		java.sql.Date d = new java.sql.Date(c.getTimeInMillis());
		return d;
	}
  /**
	 * 获取系统当前SQL类型的Timestamp
	 * 
	 * @return 当前时间
	 */
	public static Timestamp getSqlCurrentDate() {
		return new Timestamp(Clock.DEFAULT.getCurrentTimeInMillis());
	}
	public static String addMonthDate(String date,String AddType,int count){
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(dateDateFormat.parse(date));
			if(AddType.equals("month")){
				cal.add(Calendar.MONTH, count);
			}else if(AddType.equals("day")){
				cal.add(Calendar.DATE, count);
			}else{
				cal.add(Calendar.YEAR, count);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return String.valueOf(dateFormat.format(cal.getTime()));
	}
  public static void main(String[] args)  {
	  DateUtil d=new DateUtil();
//		String date = "2011-04-25 22:28:30";
		String time="20:00:00";
		String date="2017-06-09";
		System.out.println(date);
//		System.out.println("年份：" +  d.GetDateYear());
//		System.out.println("月份：" + d.GetDateMonth());
//		System.out.println("日期：" + d.GetDateDay());
		System.out.println("月初日期是: " + d.getMinMonthDate());
//		System.out.println("月末日期是: " + d.getMaxMonthDate());
//		System.out.println("是否在"+time+"点前:"+d.CheckTime(time));
//		System.out.println("是否在"+date+"点前:"+d.CheckDate(date));
//		System.out.println("是"+date+"点前:"+d.Dateequals(date,-3));
		
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(dateDateFormat.parse("2017-06-12"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
