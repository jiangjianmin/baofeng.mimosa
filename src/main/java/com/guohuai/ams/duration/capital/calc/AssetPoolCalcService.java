package com.guohuai.ams.duration.capital.calc;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetPoolCalcService {
	
	@Autowired
	private AssetPoolCalcDao poolCalcDao;

	@Transactional
	public void save(List<AssetPoolCalc> list) {
		poolCalcDao.save(list);
	}

	@Transactional
	public void saveOne(AssetPoolCalc calc) {
		poolCalcDao.save(calc);
	}
}
