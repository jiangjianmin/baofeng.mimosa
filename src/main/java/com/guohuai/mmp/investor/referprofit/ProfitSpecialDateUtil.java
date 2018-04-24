package com.guohuai.mmp.investor.referprofit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfitSpecialDateUtil {
	// 日期格式
	private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfYM = new SimpleDateFormat("yyyy-MM");
	/**
	 * 
	 * @author yihonglei
	 * @Title: getLastWeekDate
	 * @Description: 获取上周一和上周日时间
	 * @return Map<String,String>
	 * @date 2017年6月21日 上午11:47:35
	 * @since  1.0.0
	 */
	public static Map<String,String> getLastWeekDate() {
		Map<String,String> mapWeekDate = new HashMap<>();
		
		Calendar calendarLastMonday = Calendar.getInstance();
		Calendar calendarLastSunday = Calendar.getInstance();
		int dayOfWeek=calendarLastMonday.get(Calendar.DAY_OF_WEEK)-1;
		int offsetLastMonday=1-dayOfWeek;
		int offsetLastSunday=7-dayOfWeek;
		calendarLastMonday.add(Calendar.DATE, offsetLastMonday-7);
		calendarLastSunday.add(Calendar.DATE, offsetLastSunday-7);
		
		mapWeekDate.put("lastMondayDate", sdfDate.format(calendarLastMonday.getTime()));
		mapWeekDate.put("lastSundayDate", sdfDate.format(calendarLastSunday.getTime()));
		
		return mapWeekDate;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: getLastMonthDate
	 * @Description: 获取上月月初和月末的时间
	 * @return Map<String,String>
	 * @date 2017年6月21日 下午1:45:14
	 * @since  1.0.0
	 */
	public static Map<String,String> getLastMonthDate() {
		Map<String,String> mapMonthDate = new HashMap<>();
		
		Calendar lastMonthStart=Calendar.getInstance();
		lastMonthStart.add(Calendar.MONTH, -1);
		lastMonthStart.set(Calendar.DAY_OF_MONTH,1);
		
		Calendar lastMonthEnd = Calendar.getInstance();
		lastMonthEnd.set(Calendar.DAY_OF_MONTH,0);
		
		mapMonthDate.put("lastMonthStart", sdfDate.format(lastMonthStart.getTime()));
		mapMonthDate.put("lastMonthEnd", sdfDate.format(lastMonthEnd.getTime()));
		
		return mapMonthDate;
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: getLastYearMonth
	 * @Description: 获取上月日期--年月
	 * @return String
	 * @date 2017年6月21日 下午2:38:09
	 * @since  1.0.0
	 */
	public static String getLastYearMonth() {
		Calendar lastMonthStart=Calendar.getInstance();
		lastMonthStart.add(Calendar.MONTH, -1);
		lastMonthStart.set(Calendar.DAY_OF_MONTH,1);
		
		return sdfYM.format(lastMonthStart.getTime());
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: getLastDay
	 * @Description: 获取昨日日期
	 * @return String
	 * @date 2017年6月21日 下午3:10:10
	 * @since  1.0.0
	 */
	public static String getLastDay() {
		Calendar lastDay=Calendar.getInstance();
		lastDay.add(Calendar.DATE,-1);
		
		return sdfDate.format(lastDay.getTime());
	}
	
	public static void main(String[] args) {
		System.out.println("getLastWeekDate:"+getLastWeekDate());
		System.out.println("getLastMonthDate:"+getLastMonthDate());
		System.out.println("getLastYearMonth:"+getLastYearMonth());
		System.out.println("getLastDay:"+getLastDay());
	}
	
}
