package com.guohuai.ams.duration.fact.income.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.guohuai.basic.component.exception.GHException;
import com.guohuai.component.util.DateUtil;


/**
 * @author qiuliang
 *
 */
public class CheckUitls {

	public static void checkIncomeDistrDate(IncomeScheduleApplyForm form) {
		Date today = new Date();
		Date apply = null;
		try {
			apply = new SimpleDateFormat("yyyy-MM-dd").parse(form.getIncomeDistrDate());
			if(DateUtil.daysBetween(today,apply)>0){
				throw new GHException("当天可以申请当日和之后收益自动发放排期");
			}
		} catch (ParseException e) {
		}
		
	}
	public static void main(String[] args) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.set(2017,05,16);
		Date apply = new SimpleDateFormat("yyyy-MM-dd").parse("2017-06-20");
		Date today = cal.getTime();
		if(DateUtil.daysBetween(today,apply)>0){
			throw new GHException("当天可以申请当日和之后收益自动发放排期");
		}
		
	}

}
