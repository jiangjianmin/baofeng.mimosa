package com.guohuai.ams.duration.capital.calc.trust;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrustCalcService {
	
	@Autowired
	private TrustCalcDao trustCalcDao;

	@Transactional
	public void save(List<TrustCalc> list) {
		trustCalcDao.save(list);
	}
}
