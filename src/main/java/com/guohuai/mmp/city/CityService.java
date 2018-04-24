package com.guohuai.mmp.city;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.city.CityQueryAllRep.CityQueryAllRepBuilder;
import com.guohuai.mmp.city.CityQueryRep.CityQueryRepBuilder;

@Service
@Transactional
public class CityService {

	@Autowired
	private CityDao cityDao;
	
	
	/**
	 * 获取城市列表
	 * @param province true-省份 false-省份下的城市列表
	 * @param parentCode
	 * @return
	 */
	public PagesRep<CityQueryRep> findByCityParentCode(boolean province, String parentCode) {
		List<CityEntity> citys = new ArrayList<CityEntity>();
		if (province) {
			citys = this.cityDao.getProvinces();
		} else {
			citys = this.cityDao.findByCityParentCode(parentCode);
		}
		
		PagesRep<CityQueryRep> pages = new PagesRep<CityQueryRep>();
		for (CityEntity city : citys) {
			CityQueryRep rep = new CityQueryRepBuilder()
					.value(city.getCityCode())
					.text(city.getCityName())
					.build();
			pages.add(rep);
		}
		pages.setTotal(citys.size());
		return pages;
	}
	
	/**
	 * 获取所有城市列表
	 * @return
	 */
	public PagesRep<CityQueryAllRep> getAllCitys() {
		
		PagesRep<CityQueryAllRep> pages = new PagesRep<CityQueryAllRep>();
		
		List<CityEntity> provinces = this.cityDao.getProvinces();
		for (CityEntity province : provinces) {
			CityQueryAllRep allRep = new CityQueryAllRepBuilder()
					.value(province.getCityCode())
					.text(province.getCityName())
					.build();
			
			List<CityEntity> citys = this.cityDao.findByCityParentCode(province.getCityCode());
			
			List<CityQueryRep> reps = new ArrayList<CityQueryRep>();
			for (CityEntity city : citys) {
				CityQueryRep rep = new CityQueryRepBuilder()
						.value(city.getCityCode())
						.text(city.getCityName())
						.build();
				reps.add(rep);
			}
			allRep.setChildren(reps);
			pages.add(allRep);
		}
		return pages;
	}
}

