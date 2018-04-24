package com.guohuai.calendar;

import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.calendar.TradeCalendar;
import com.guohuai.ams.calendar.TradeCalendarTService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TradeCalendarService {

	@Autowired
	private TradeCalendarTService tradeCalendarTService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	
	private static LinkedList<TradeCalendar> CALENDARS = new LinkedList<TradeCalendar>();
	private static Map<String, Integer> INDEXES = new HashMap<String, Integer>();

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadLock readLock = this.lock.readLock();
	private WriteLock writeLock = this.lock.writeLock();

	@PostConstruct
	public void init() {
		try {
			this.writeLock.lock();
			CALENDARS.clear();
			INDEXES.clear();
			List<TradeCalendar> calendars = this.tradeCalendarTService.findAll();

			for (int i = 0; i < calendars.size(); i++) {
				TradeCalendar c = calendars.get(i);
				CALENDARS.add(c);
				INDEXES.put(DateUtil.formatDate(c.getCalendarDate().getTime()), i);
			}
			log.info("calendar size:" + CALENDARS.size());
		} finally {
			this.writeLock.unlock();
		}
	}
	
	public void taskTradeCalendar() {

		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_tradeCalendar)) {
			JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_tradeCalendar);
			try {
				init();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				jobLog.setJobMessage(AMPException.getStacktrace(e));
				jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
			}
			jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
			this.jobLogService.saveEntity(jobLog);
			this.jobLockService.resetJob(JobLockEntity.JOB_jobId_tradeCalendar);
		}
	}

	public boolean isTrade(Date date) {

		try {
			this.readLock.lock();
			Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));

			if (null == index) {
				// error.define[30201]=未知的交易日期
				throw AMPException.getException(30201);
			}

			TradeCalendar c = CALENDARS.get(index);

			return c.getIsOpen() == 1;
		} finally {
			this.readLock.unlock();
		}

	}
	
	public Date getPrevTradeDate(Date date) {
		Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));
		if (null == index) {
			// error.define[30201]=未知的交易日期
			throw AMPException.getException(30201);
		}
		TradeCalendar c = CALENDARS.get(index);
		return c.getPrevTradeDate();
	}

	public Date nextTrade(Date date) {

		try {
			this.readLock.lock();
			return this.nextTrade(date, 1);
		} finally {
			this.readLock.unlock();
		}
	}

	public Date nextTrade(Date date, int skip) {
		log.info("parameters: {}, {}", date, skip);
		try {
			this.readLock.lock();

			Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));

			if (null == index) {
				// error.define[30202]=未加载的交易日历
				throw AMPException.getException(30201);
			}

			index = index + 1;

			if (index > CALENDARS.size() - 1) {
				// error.define[30203]=越界的交易日期
				throw AMPException.getException(30203);
			}

			if (skip <= 0) {
				skip = 1;
			}

			int skipped = 1;

			for (int i = index; i < CALENDARS.size(); i++) {
				TradeCalendar c = CALENDARS.get(i);
				if (c.getIsOpen() == 1) {
					if (skipped == skip) {
						return c.getCalendarDate();
					} else {
						skipped++;
					}
				}
			}
			// error.define[30203]=越界的交易日期
			throw AMPException.getException(30203);

		} finally {
			this.readLock.unlock();
		}

	}

	public Date lastTrade(Date date) {

		try {
			this.readLock.lock();

			return this.lastTrade(date, 0);
		} finally {
			this.readLock.unlock();
		}

	}

	public Date lastTrade(Date date, int skip) {

		try {
			this.readLock.lock();

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
				if (c.getIsOpen() == 1) {
					if (skipped == skip) {
						return c.getCalendarDate();
					} else {
						skipped++;
					}
				}
			}

			throw AMPException.getException(30203);

		} finally {
			this.readLock.unlock();
		}

	}



//	public boolean isWork(Date date) {
//
//		try {
//			this.readLock.lock();
//
//			Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));
//
//			if (null == index) {
//				throw AMPException.getException(30201);
//			}
//
//			if (index > CALENDARS.size() - 1) {
//				throw AMPException.getException(30203);
//			}
//
//			TradeDateObj c = CALENDARS.get(index);
//
//			return c.getIsWork() == 1;
//
//		} finally {
//			this.readLock.unlock();
//		}
//
//	}

//	public Date nextWork(Date date) {
//
//		try {
//			this.readLock.lock();
//			return this.nextWork(date, 0);
//
//		} finally {
//			this.readLock.unlock();
//		}
//
//	}

//	public Date nextWork(Date date, int skip) {
//		try {
//			this.readLock.lock();
//			if (skip == -1) {
//				if (this.isWork(date)) {
//					return date;
//				} else {
//					return this.nextWork(date);
//				}
//			}
//
//			Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));
//
//			if (null == index) {
//				throw AMPException.getException(30201);
//			}
//
//			index = index + 1;
//
//			if (index > CALENDARS.size() - 1) {
//				throw AMPException.getException(30203);
//			}
//
//			if (skip < 0) {
//				skip = 0;
//			}
//
//			int skipped = 0;
//
//			for (int i = index; i < CALENDARS.size(); i++) {
//				TradeDateObj c = CALENDARS.get(i);
//				if (c.getIsWork() == 1) {
//					if (skipped == skip) {
//						return c.getCalendarDate();
//					} else {
//						skipped++;
//					}
//				}
//			}
//
//			throw AMPException.getException(30203);
//
//		} finally {
//			this.readLock.unlock();
//		}
//	}

//	public Date lastWork(Date date) {
//		try {
//			this.readLock.lock();
//			return this.lastWork(date, 0);
//		} finally {
//			this.readLock.unlock();
//		}
//	}

//	public Date lastWork(Date date, int skip) {
//		try {
//			this.readLock.lock();
//			if (skip == -1) {
//				if (this.isWork(date)) {
//					return date;
//				} else {
//					return this.lastWork(date);
//				}
//			}
//
//			Integer index = INDEXES.get(DateUtil.formatDate(date.getTime()));
//
//			if (null == index) {
//				throw AMPException.getException(30201);
//			}
//
//			index = index - 1;
//
//			if (index < 0) {
//				throw AMPException.getException(30203);
//			}
//
//			if (skip < 0) {
//				skip = 0;
//			}
//
//			int skipped = 0;
//
//			for (int i = index; i >= 0; i--) {
//				TradeDateObj c = CALENDARS.get(i);
//				if (c.getIsOpen() == 1) {
//					if (skipped == skip) {
//						return c.getCalendarDate();
//					} else {
//						skipped++;
//					}
//				}
//			}
//
//			throw AMPException.getException(30203);
//		} finally {
//			this.readLock.unlock();
//		}
//	}

//	public List<Date> workDates(Date begin, Date end) {
//		try {
//			this.readLock.lock();
//			Integer mini = INDEXES.get(DateUtil.formatDate(begin.getTime()));
//
//			if (null == mini) {
//				throw AMPException.getException(30201);
//			}
//
//			if (mini < 0) {
//				throw AMPException.getException(30203);
//			}
//
//			Integer maxi = INDEXES.get(DateUtil.formatDate(end.getTime()));
//
//			if (null == maxi) {
//				throw AMPException.getException(30201);
//			}
//
//			if (maxi < 0) {
//				throw AMPException.getException(30203);
//			}
//
//			List<Date> dates = new ArrayList<Date>();
//			if (maxi >= mini) {
//				for (int i = mini; i <= maxi; i++) {
//					TradeDateObj tc = CALENDARS.get(i);
//					if (tc.getIsWork() == 1) {
//						dates.add(tc.getCalendarDate());
//					}
//				}
//			}
//
//			return dates;
//		} finally {
//			this.readLock.unlock();
//		}
//	}

}
