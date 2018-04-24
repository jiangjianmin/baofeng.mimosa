package com.guohuai.ams.calendar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface TradeCalendarDao extends JpaRepository<TradeCalendar, String>, JpaSpecificationExecutor<TradeCalendar> {

	@Query(value = "SELECT * FROM T_TRADE_CALENDAR WHERE calendarDate BETWEEN DATE_SUB(NOW(), INTERVAL 15 DAY) AND DATE_ADD(NOW(), INTERVAL 30 DAY) ORDER BY calendarDate ASC", nativeQuery = true)
	public List<TradeCalendar> findRecently();

}
