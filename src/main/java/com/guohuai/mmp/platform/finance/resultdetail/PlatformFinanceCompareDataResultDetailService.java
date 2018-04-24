package com.guohuai.mmp.platform.finance.resultdetail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.mmp.platform.finance.result.PlatformFinanceCompareDataResultEntity;

@Service
public class PlatformFinanceCompareDataResultDetailService {
	
	@Autowired
	PlatformFinanceCompareDataResultDetailDao platformFinanceCompareDataResultDetailDao;
	
	public void save(List<PlatformFinanceCompareDataResultEntity> compareDataResultList) {
		List<PlatformFinanceCompareDataResultDetailEntity> list = new ArrayList<PlatformFinanceCompareDataResultDetailEntity>();
		PlatformFinanceCompareDataResultDetailEntity e = null;
		try {
			for (PlatformFinanceCompareDataResultEntity entity : compareDataResultList) {
				e = new PlatformFinanceCompareDataResultDetailEntity();
				BeanUtils.copyProperties(e, entity);
				list.add(e);
			}
			platformFinanceCompareDataResultDetailDao.save(list);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
