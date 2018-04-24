package com.guohuai.ams.product.reward;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProductIncomeRewardSnapshotService {

	@Autowired
	private ProductIncomeRewardSnapshotDao productIncomeRewardSnapshotDao;
	
	public static Map<Date, List<ProductIncomeRewardSnapshot>> expire = new HashMap<Date, List<ProductIncomeRewardSnapshot>>();
	
	public void snapShotProductIncomeReward(String productOid, Date incomeDate) {
		if(productIncomeRewardSnapshotDao.countByProductOidAndSnapshotDate(productOid, incomeDate) == 0) {
			productIncomeRewardSnapshotDao.snapshotProductIncomeReward(productOid, incomeDate);
		}
	}
	//每日清空
	public List<ProductIncomeRewardSnapshot> findBySnapshotDate(Date snapshotDate) {
		if(!expire.containsKey(snapshotDate)) {
			expire.clear();
			expire.put(snapshotDate, productIncomeRewardSnapshotDao.findBySnapshotDate(snapshotDate));
		} 
		return expire.get(snapshotDate);
	}
}
