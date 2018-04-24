package com.guohuai.ams.duration.assetPool.history;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.assetPool.chargefee.AssetPoolFeeSettingService;
import com.guohuai.ams.duration.order.OrderService;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;

/**
 * 资产池历史估值表
 * 
 * @author star.zhu
 *         2016年6月30日
 */
@Service
public class HistoryValuationService {

	@Autowired
	private HistoryValuationDao historyDao;
	@Autowired
	private OrderService orderService;
	@Autowired
	private AssetPoolFeeSettingService feeSettingService;

	@Transactional
	public void save(HistoryValuationEntity entity) {
		historyDao.save(entity);
	}

	/**
	 * 创建资产池昨日估值历史
	 * 
	 * @param pid
	 * @param valuation
	 */
	@Transactional
	public void createHistory(String pid, BigDecimal valuation, int basic) {
		HistoryValuationEntity entity = new HistoryValuationEntity();
		entity.setOid(StringUtil.uuid());
		entity.setAssetPoolOid(pid);
		// 基准日
		Date date = new Date(DateUtil.addDay(new java.util.Date(System.currentTimeMillis()), -1).getTime());
		entity.setBaseDate(date);
		entity.setScale(valuation);
		entity.setTrade(orderService.navData(pid, date));
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
		//
		BigDecimal value = feeSettingService.feeCalc(pid, valuation, basic);
		entity.setFeeValue(value);

		historyDao.save(entity);
	}

	@Transactional
	public HistoryValuationEntity findByAssetPoolAndDate(String assetPoolOid, Date baseDate) {
		List<HistoryValuationEntity> list = this.historyDao.findByAssetPoolOidAndBaseDate(assetPoolOid, baseDate);
		if (null == list || list.size() == 0) {
			return null;
		}
		return list.get(0);
	}
	
	/**
	 * 查询当前资产池的历史估值记录
	 * @param assetPoolOid
	 * @param pageable
	 * @return
	 */
	@Transactional
	public PageResp<HistoryValuationEntity> findByPid(Specification<HistoryValuationEntity> spec, Pageable pageable) {
		PageResp<HistoryValuationEntity> resp = new PageResp<HistoryValuationEntity>();
		Page<HistoryValuationEntity> list = historyDao.findAll(spec, pageable);
		if (null != list && !list.getContent().isEmpty()) {
			resp.setRows(list.getContent());
			resp.setTotal(list.getTotalElements());
		}
		return resp;	
	}
}
