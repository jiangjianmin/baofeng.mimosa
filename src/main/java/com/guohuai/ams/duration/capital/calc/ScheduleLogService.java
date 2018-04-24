package com.guohuai.ams.duration.capital.calc;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleLogService {
	
	@Autowired
	private ScheduleLogDao logDao;

	@Transactional
	public void save(ScheduleLog log) {
		logDao.save(log);
	}
}
