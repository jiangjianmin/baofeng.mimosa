package com.guohuai.ams.duration.fact.calibration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.order.SPVOrder;
import com.guohuai.component.util.StringUtil;

@Service
public class MarketAdjustOrderService {

	@Autowired
	private MarketAdjustOrderDao marketAdjustOrderDao;

	@Transactional
	public boolean save(MarketAdjustEntity adjust, List<SPVOrder> orders) {

		boolean edit = false;

		List<MarketAdjustOrderEntity> his = this.marketAdjustOrderDao.findByAdjust(adjust);
		Map<String, MarketAdjustOrderEntity> hisMap = new HashMap<String, MarketAdjustOrderEntity>();
		if (null != his && his.size() > 0) {
			for (MarketAdjustOrderEntity h : his) {
				hisMap.put(h.getOrder().getOid(), h);
			}
		}

		for (SPVOrder o : orders) {
			if (!hisMap.containsKey(o.getOid())) {
				MarketAdjustOrderEntity e = new MarketAdjustOrderEntity();
				e.setOid(StringUtil.uuid());
				e.setAdjust(adjust);
				e.setOrder(o);
				this.marketAdjustOrderDao.save(e);
				edit = true;
			} else {
				hisMap.remove(o.getOid());
			}
		}

		if (hisMap.size() > 0) {
			for (String key : hisMap.keySet()) {
				MarketAdjustOrderEntity e = hisMap.get(key);
				this.marketAdjustOrderDao.delete(e);
				edit = true;
			}
		}

		return edit;
	}

	@Transactional
	public List<MarketAdjustOrderEntity> read(MarketAdjustEntity adjust) {
		return this.marketAdjustOrderDao.findByAdjust(adjust);
	}

}
