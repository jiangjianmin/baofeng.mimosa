package com.guohuai.ams.calendar;



import java.sql.Date;

import org.hibernate.validator.constraints.NotBlank;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TradeCalendarAddReq {
	
	private String oid;
	
	@NotBlank(message = "类型不能为空！")	
	private String exchangeCD;
	private Date calendarDate;
	private Integer isOpen;
	private Integer isWork;
	private Date prevTradeDate;
	private Date prevWorkDate;
	private Integer isWeekEnd;
	private Integer isMonthEnd;
	private Integer isQuarterEnd;
	private Integer isYearEnd;
	
	
}
