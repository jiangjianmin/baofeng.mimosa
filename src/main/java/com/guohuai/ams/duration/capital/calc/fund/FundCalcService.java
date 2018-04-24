package com.guohuai.ams.duration.capital.calc.fund;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FundCalcService {
	
	@Autowired
	private FundCalcDao fundCalcDao;

	@Transactional
	public void save(List<FundCalc> list) {
		fundCalcDao.save(list);
	}
}
