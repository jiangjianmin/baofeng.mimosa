package com.guohuai.mmp.platform.finance.data;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.guohuai.component.util.DateUtil;


@Service
@Transactional
public class PlatformFinanceCompareDataNewService {
	@Autowired
	private PlatformFinanceCompareDataDao financeCompareDataDao;
	

	public void save(List<PlatformFinanceCompareDataEntity> list){
		this.financeCompareDataDao.save(list);
	}

	public void deleteByBuzzDate(String date) {
		financeCompareDataDao.deleteByBuzzDate(DateUtil.parseToSqlDate(date));
	}


}
