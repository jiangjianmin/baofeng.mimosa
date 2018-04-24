package com.guohuai.ams.calendar;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;


@Service
public class TradeCalendarTService {

	@Autowired
	private TradeCalendarDao daoTradeCalendar;

	private static LinkedList<TradeCalendar> CALENDARS = new LinkedList<TradeCalendar>();
	private static Map<String, Integer> INDEXES = new HashMap<String, Integer>();

	@PostConstruct
	public void initCache() {
		CALENDARS.clear();
		INDEXES.clear();
		List<TradeCalendar> calendars = this.findAll();

		for (int i = 0; i < calendars.size(); i++) {
			TradeCalendar c = calendars.get(i);
			CALENDARS.add(c);
			INDEXES.put(DateUtil.formatDate(c.getCalendarDate().getTime()), i);
		}
	}

	@Transactional
	public List<TradeCalendar> findAll() {
		List<TradeCalendar> calendars = this.daoTradeCalendar.findAll(new Sort(new Order(Direction.ASC, "calendarDate")));
		if (calendars.size() == 0) {
			throw AMPException.getException(30202);
		}
		return calendars;
	}

	public boolean isTrade(Date date) {

		Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));

		if (null == index) {
			throw AMPException.getException(30201);
		}

		if (index > CALENDARS.size() - 1) {
			throw AMPException.getException(30203);
		}

		TradeCalendar c = CALENDARS.get(index);

		return c.getIsOpen() == TradeCalendar.IS_OPEN_Yes;

	}

	public Date nextTrade(Date date) {
		return this.nextTrade(date, 0);
	}

	public Date nextTrade(Date date, int skip) {

		if (skip == -1) {
			if (this.isTrade(date)) {
				return date;
			} else {
				return this.nextTrade(date);
			}
		}

		Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));

		if (null == index) {
			throw AMPException.getException(30201);
		}

		index = index + 1;

		if (index > CALENDARS.size() - 1) {
			throw AMPException.getException(30203);
		}

		if (skip < 0) {
			skip = 0;
		}

		int skipped = 0;

		for (int i = index; i < CALENDARS.size(); i++) {
			TradeCalendar c = CALENDARS.get(i);
			if (c.getIsOpen() == TradeCalendar.IS_OPEN_Yes) {
				if (skipped == skip) {
					return c.getCalendarDate();
				} else {
					skipped++;
				}
			}
		}

		throw AMPException.getException(30203);
	}

	public Date lastTrade(Date date) {
		return this.lastTrade(date, 0);
	}

	public Date lastTrade(Date date, int skip) {

		if (skip == -1) {
			if (this.isTrade(date)) {
				return date;
			} else {
				return this.lastTrade(date);
			}
		}

		Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));

		if (null == index) {
			throw AMPException.getException(30201);
		}

		index = index - 1;

		if (index < 0) {
			throw AMPException.getException(30203);
		}

		if (skip < 0) {
			skip = 0;
		}

		int skipped = 0;

		for (int i = index; i >= 0; i--) {
			TradeCalendar c = CALENDARS.get(i);
			if (c.getIsOpen() == TradeCalendar.IS_OPEN_Yes) {
				if (skipped == skip) {
					return c.getCalendarDate();
				} else {
					skipped++;
				}
			}
		}

		throw AMPException.getException(30203);

	}

	public boolean isWork(Date date) {

		Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));

		if (null == index) {
			throw AMPException.getException(30201);
		}

		if (index > CALENDARS.size() - 1) {
			throw AMPException.getException(30203);
		}

		TradeCalendar c = CALENDARS.get(index);

		return c.getIsWork() == TradeCalendar.IS_WORK_Yes;

	}

	public Date nextWork(Date date) {
		return this.nextWork(date, 0);
	}

	public Date nextWork(Date date, int skip) {

		if (skip == -1) {
			if (this.isWork(date)) {
				return date;
			} else {
				return this.nextWork(date);
			}
		}

		Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));

		if (null == index) {
			throw AMPException.getException(30201);
		}

		index = index + 1;

		if (index > CALENDARS.size() - 1) {
			throw AMPException.getException(30203);
		}

		if (skip < 0) {
			skip = 0;
		}

		int skipped = 0;

		for (int i = index; i < CALENDARS.size(); i++) {
			TradeCalendar c = CALENDARS.get(i);
			if (c.getIsWork() == TradeCalendar.IS_WORK_Yes) {
				if (skipped == skip) {
					return c.getCalendarDate();
				} else {
					skipped++;
				}
			}
		}

		throw AMPException.getException(30203);
	}

	public Date lastWork(Date date) {
		return this.lastWork(date, 0);
	}

	public Date lastWork(Date date, int skip) {

		if (skip == -1) {
			if (this.isWork(date)) {
				return date;
			} else {
				return this.lastWork(date);
			}
		}

		Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));

		if (null == index) {
			throw AMPException.getException(30201);
		}

		index = index - 1;

		if (index < 0) {
			throw AMPException.getException(30203);
		}

		if (skip < 0) {
			skip = 0;
		}

		int skipped = 0;

		for (int i = index; i >= 0; i--) {
			TradeCalendar c = CALENDARS.get(i);
			if (c.getIsOpen() == TradeCalendar.IS_WORK_Yes) {
				if (skipped == skip) {
					return c.getCalendarDate();
				} else {
					skipped++;
				}
			}
		}

		throw AMPException.getException(30203);

	}

	public List<Date> workDates(Date begin, Date end) {

		Integer mini = INDEXES.get(DateUtil.formatDate(begin.getTime()));

		if (null == mini) {
			throw AMPException.getException(30201);
		}

		if (mini < 0) {
			throw AMPException.getException(30203);
		}

		Integer maxi = INDEXES.get(DateUtil.formatDate(end.getTime()));

		if (null == maxi) {
			throw AMPException.getException(30201);
		}

		if (maxi < 0) {
			throw AMPException.getException(30203);
		}

		List<Date> dates = new ArrayList<Date>();
		if (maxi >= mini) {
			for (int i = mini; i <= maxi; i++) {
				TradeCalendar tc = CALENDARS.get(i);
				if (tc.getIsWork() == TradeCalendar.IS_WORK_Yes) {
					dates.add(tc.getCalendarDate());
				}
			}
		}

		return dates;
	}

	/**
	 * 分页查询
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public TradeCalendarListResp queryPages(Specification<TradeCalendar> spec, Pageable pageable) {
		Page<TradeCalendar> pages = this.daoTradeCalendar.findAll(spec, pageable);
		
		TradeCalendarListResp resp = new TradeCalendarListResp();
		resp.setTotal(pages.getTotalElements());
		resp.setRows(pages.getContent());
		
		return resp;
	}

	/**
	 * 删除
	 * @param oid
	 */
	public void delete(String oid) {
		TradeCalendar en = getByOid(oid);
		this.daoTradeCalendar.delete(en);
	}
	
	/**
	 * 获取实体
	 * @param oid
	 * @return
	 */
	public TradeCalendar getByOid(String oid){
		TradeCalendar en = this.daoTradeCalendar.findOne(oid);
		if (en == null){
			// 交易日历不存在
			throw AMPException.getException(30204);
		}
		return en;
	}

	// 添加
	public void add(TradeCalendarAddReq req) {
		TradeCalendar en = null;
		
		if (req.getOid() != null && !req.getOid().isEmpty()){
			en = getByOid(req.getOid());
		}else{
			en = this.daoTradeCalendar.findOne(req.getExchangeCD()+req.getCalendarDate());
			if (en != null){
				// 交易日历已存在
				throw AMPException.getException(30205);
			}
			en = new TradeCalendar();
			en.setOid(req.getExchangeCD()+req.getCalendarDate());
		}
		
		en.setExchangeCD(req.getExchangeCD());
		en.setIsOpen(req.getIsOpen());
		en.setIsWork(req.getIsWork());
		en.setCalendarDate(req.getCalendarDate());
		en.setIsWeekEnd(req.getIsWeekEnd());
		en.setIsMonthEnd(req.getIsMonthEnd());
		en.setIsQuarterEnd(req.getIsQuarterEnd());
		en.setIsYearEnd(req.getIsYearEnd());
		en.setPrevTradeDate(req.getPrevTradeDate());
		en.setPrevWorkDate(req.getPrevWorkDate());
		
		this.daoTradeCalendar.saveAndFlush(en);
	}
}
