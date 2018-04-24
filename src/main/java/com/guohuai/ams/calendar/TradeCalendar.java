package com.guohuai.ams.calendar;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.guohuai.component.util.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_TRADE_CALENDAR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCalendar {

	public static final int IS_OPEN_Yes = 1;
	public static final int IS_OPEN_No = 0;

	public static final int IS_WORK_Yes = 1;
	public static final int IS_WORK_No = 0;

	/** 是否是周末，这个只有周五，才是1，不是按自然的周六日算 */
	public static final int IS_WEEK_END_Yes = 1;
	public static final int IS_WEEK_END_No = 0;

	public static final int IS_MONTH_END_Yes = 1;
	public static final int IS_MONTH_END_No = 0;

	public static final int IS_YEAR_END_Yes = 1;
	public static final int IS_YEAR_END_No = 0;

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid.hex")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid")
	private String oid;
	private String exchangeCD;
	private Date calendarDate;
	private int isOpen;
	private int isWork;
	private Date prevTradeDate;
	private Date prevWorkDate;
	private int isWeekEnd;
	private int isMonthEnd;
	private int isQuarterEnd;
	private int isYearEnd;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TradeCalendar) {
			TradeCalendar target = (TradeCalendar) obj;
			return null == this.getCalendarDate() ? null == target.getCalendarDate() : DateUtil.same(this.getCalendarDate(), target.getCalendarDate());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return null == this.getCalendarDate() ? 0 : this.getCalendarDate().hashCode();
	}

}
