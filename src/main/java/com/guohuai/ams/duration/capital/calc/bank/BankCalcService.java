package com.guohuai.ams.duration.capital.calc.bank;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankCalcService {
	
	@Autowired
	private BankCalcDao bankCalcDao;

	@Transactional
	public void save(List<BankCalc> list) {
		bankCalcDao.save(list);
	}
}
