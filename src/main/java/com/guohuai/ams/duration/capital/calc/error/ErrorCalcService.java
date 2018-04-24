package com.guohuai.ams.duration.capital.calc.error;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ErrorCalcService {
	
	@Autowired
	private ErrorCalcDao errorCalcDao;

	@Transactional
	public void save(List<ErrorCalc> list) {
		errorCalcDao.save(list);
	}
}
