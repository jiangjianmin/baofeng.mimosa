package com.guohuai.ams.product.reward;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.dict.Dict;
import com.guohuai.ams.product.Product;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProductIncomeRewardCacheService {

	public static Map<String, List<ProductIncomeReward>> cache = new HashMap<String, List<ProductIncomeReward>>();

	public static Map<String, Long> expire = new HashMap<String, Long>();

	@Autowired
	private ProductIncomeRewardDao productIncomeRewardDao;

	/**
	 * 
	 * @param key
	 *            产品oid
	 * @param holdDays
	 *            持仓天数
	 * @return
	 */
	@Transactional
	public BigDecimal getReward(String key, int holdDays) {
		List<ProductIncomeReward> rs = this.readCache(key);
		if (null == rs || rs.size() == 0) {
			return BigDecimal.ZERO;
		}

		for (ProductIncomeReward r : rs) {
			if (null == r.getEndDate() || r.getEndDate() <= 0) {
				if (holdDays >= r.getStartDate()) {
					return r.getRatio();
				}
			} else {
				if (holdDays >= r.getStartDate() && holdDays <= r.getEndDate()) {
					return r.getRatio();
				}
			}
		}

		return BigDecimal.ZERO;
	}

	public Map<ProductIncomeReward, PracticeEntity> getAllRewardEntity(Product product, java.sql.Date incomeDate) {
		Map<ProductIncomeReward, PracticeEntity> map = new HashMap<ProductIncomeReward, PracticeEntity>();
		List<ProductIncomeReward> rs = this.readCache(product.getOid());
		if (null == rs || rs.size() == 0) {
			return map;
		}

		for (ProductIncomeReward reward : rs) {
			PracticeEntity practice = PracticeEntity.builder().product(product).reward(reward).tDate(incomeDate).updateTime(DateUtil.getSqlCurrentDate()).createTime(DateUtil.getSqlCurrentDate())
					.totalHoldVolume(BigDecimal.ZERO).totalRewardIncome(BigDecimal.ZERO).build();
			map.put(reward, practice);
		}

		return map;
	}

	public ProductIncomeReward getRewardEntity(String key, int holdDays) {
		List<ProductIncomeReward> rs = this.readCache(key);
		if (null == rs || rs.size() == 0) {
			return null;
		}

		for (ProductIncomeReward r : rs) {
			if (null == r.getEndDate() || r.getEndDate() <= 0) {
				if (holdDays >= r.getStartDate()) {
					return r;
				}
			} else {
				if (holdDays >= r.getStartDate() && holdDays <= r.getEndDate()) {
					return r;
				}
			}
		}

		return null;
	}

	@Transactional
	public List<ProductIncomeReward> readCache(String key) {
		long now = System.currentTimeMillis();
		if (!expire.containsKey(key) || now - expire.get(key) > 1000 * 60 * 15) {
			return this.reload(key);
		}
		return cache.get(key);
	}
	

	@Transactional
	private List<ProductIncomeReward> reload(String key) {
		List<ProductIncomeReward> rs = this.productIncomeRewardDao.findByProductOid(key);
		List<ProductIncomeReward> list = new ArrayList<ProductIncomeReward>();
		Product product = null;
		Dict dict = new Dict();
		for (ProductIncomeReward pir : rs) {
			ProductIncomeReward reward = new ProductIncomeReward();
			reward.setOid(pir.getOid());
			reward.setRatio(pir.getRatio());
			reward.setStartDate(pir.getStartDate());
			reward.setDratio(pir.getDratio());
			reward.setEndDate(pir.getEndDate());
			reward.setLevel(pir.getLevel());
			if(product == null) {
				product = new Product();
				product.setOid(pir.getProduct().getOid());
				product.setName(pir.getProduct().getName());
				product.setFullName(pir.getProduct().getFullName());
				dict.setOid(pir.getProduct().getType().getOid()); 
				product.setType(dict);
			}
			reward.setProduct(product);// 不要再通过product级联其他对象 防止懒加载异常
			list.add(reward);
		}
		expire.put(key, System.currentTimeMillis());
		cache.put(key, list);
		return list;
	}
	
	public boolean hasRewardIncome(String productOid){
		return readCache(productOid).size() > 0;
	}

	public List<String> getAwardProductOid() {
		return this.productIncomeRewardDao.getRewardProductOid();
	}
}
