package com.guohuai.ams.duration.fact.calibration;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.guohuai.ams.acct.books.document.SPVDocumentService;
import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.order.SPVOrder;
import com.guohuai.ams.order.SPVOrderService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;

/**
 * 市值校准
 * 
 * @author star.zhu
 *         2016年6月16日
 */
@Service
public class MarketAdjustService {

	@Autowired
	private MarketAdjustDao adjustDao;

	@Autowired
	private MarketAdjustOrderService marketAdjustOrderService;

	@Autowired
	private SPVOrderService spvOrderService;

	@Autowired
	private SPVDocumentService spvDocumentService;

	@Autowired
	private AssetPoolService assetPoolService;

	@Transactional
	public void save(MarketAdjustEntity entity) {
		adjustDao.save(entity);
	}

	@Transactional
	public MarketAdjustEntity findOne(String oid) {
		return adjustDao.findOne(oid);
	}

	/**
	 * 录入市值校准记录
	 * 
	 * @param form
	 */
	@Transactional
	public void saveMarketAdjust(MarketAdjustForm form, String operator) {
		MarketAdjustEntity entity = this.adjustDao.findOne(form.getOid());
		if (null == entity) {
			throw new AMPException("操作已失效, 请刷新后重试.");
		}

		if (!entity.getAversion().equals(form.getAversion())) {
			throw new AMPException("SPV订单状态已变更, 请刷新后重试.");
		}

		entity.setShares(BigDecimalUtil.formatForMul10000(form.getShares()));
		entity.setNav(form.getNav());
		entity.setProfit(BigDecimalUtil.formatForMul10000(form.getProfit()));
		entity.setRatio(BigDecimalUtil.formatForDivide100(form.getRatio()));

		entity.setStatus(MarketAdjustEntity.CREATE);
		entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
		entity.setCreator(operator);

		// AssetPoolEntity pool = this.assetPoolService.getByOid(form.getAssetpoolOid());
		// BigDecimal deviationValue = entity.getShares().multiply(entity.getNav()).subtract(pool.getScale()).setScale(4, BigDecimal.ROUND_HALF_UP);
		// pool.setDeviationValue(deviationValue);
		// this.assetPoolService.save(pool);

		this.save(entity);
	}

	/**
	 * 市值校准录入审核
	 * 
	 * @param oid
	 * @param type
	 * @param operator
	 */

	@Transactional
	public void auditMarketAdjust(String oid, String type, String operator) {
		MarketAdjustEntity adjust = this.adjustDao.findOne(oid);

		// 判断是否是隔天审核
		Date baseDate = this.getBaseDate();
		if (!DateUtil.formatDate(baseDate.getTime()).equals(DateUtil.formatDate(adjust.getBaseDate().getTime()))) {
			throw new AMPException("该申请已失效!");
		}

		if ("pass".equals(type)) {

			// 更新资产池
			AssetPoolEntity assetPool = adjust.getAssetPool();
			assetPool.setBaseDate(adjust.getBaseDate());
			assetPool.setShares(adjust.getShares());
			assetPool.setNav(adjust.getNav());
			assetPool.setMarketValue(adjust.getShares().multiply(adjust.getNav().setScale(4, BigDecimal.ROUND_HALF_UP)));
			assetPool.setDeviationValue(adjust.getShares().multiply(adjust.getNav()).subtract(assetPool.getScale()).setScale(4, BigDecimal.ROUND_HALF_UP));
			this.assetPoolService.save(assetPool);

			// 更新SPV订单
			List<MarketAdjustOrderEntity> orders = this.marketAdjustOrderService.read(adjust);
			if (null != orders && orders.size() > 0) {
				for (MarketAdjustOrderEntity order : orders) {
					SPVOrder o = order.getOrder();
					this.spvOrderService.updateEntryStatus(o.getOid());
				}
			}

			if (null != adjust.getProfit() && adjust.getProfit().compareTo(BigDecimal.ZERO) != 0) {
				this.spvDocumentService.incomeConfirm(assetPool.getOid(), oid, adjust.getProfit());
			}

			adjust.setStatus(MarketAdjustEntity.PASS);
		} else {
			adjust.setStatus(MarketAdjustEntity.FAIL);
		}
		adjust.setAuditor(operator);
		adjust.setAuditTime(new Timestamp(System.currentTimeMillis()));
		this.adjustDao.save(adjust);
	}

	/**
	 * 查询当天的订单状态
	 * -1：未审核；0：未录入；1：已通过
	 * 
	 * @param pid
	 * @return
	 */
	@Transactional
	public int getMarketAdjustStatus(String pid) {
		// 基准日为T-1日
		java.util.Date date = DateUtil.addDay(new Date(System.currentTimeMillis()), -1);
		MarketAdjustEntity entity = adjustDao.findByBaseDate(pid, new Date(date.getTime()));
		if (null != entity) {
			if (MarketAdjustEntity.CREATE.equals(entity.getStatus())) {
				return -1;
			} else {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * 市值校准记录 列表
	 * 
	 * @param pid
	 * @param pageable
	 * @return
	 */
	@Transactional
	public Page<MarketAdjustEntity> getMarketAdjustList(String pid, Pageable pageable) {
		pid = assetPoolService.getPid(pid);
		Page<MarketAdjustEntity> list = adjustDao.getListByPid(pid, pageable);

		return list;
	}

	/**
	 * 获取收益率列表
	 * 
	 * @param pid
	 * @return
	 */
	@Transactional
	public List<JSONObject> getListForYield(String pid) {
		List<JSONObject> objList = Lists.newArrayList();
		List<MarketAdjustEntity> list = adjustDao.getListForYield(pid);
		if (null != list && !list.isEmpty()) {
			JSONObject obj = null;
			for (MarketAdjustEntity entity : list) {
				if (entity.getRatio().equals(-999.9900)) {
					continue;
				}
				obj = new JSONObject();
				obj.put("date", entity.getBaseDate());
				obj.put("yield", entity.getRatio());
				obj.put("marketValue", entity.getShares().multiply(entity.getNav()));
				objList.add(obj);
			}
		}
		return objList;
	}

	/**
	 * 市值校准录入 详情表单
	 * 
	 * @param oid
	 * @param pid
	 * @return
	 */
	@Transactional
	public MarketAdjustResp getMarketAdjust(String pid) {
		AssetPoolEntity assetPool = this.assetPoolService.getByOid(pid);
		Date baseDate = this.getBaseDate();
		MarketAdjustEntity adjust = this.adjustDao.findByAssetPoolAndBaseDate(assetPool, baseDate);
		if (null != adjust && StringUtil.in(adjust.getStatus(), MarketAdjustEntity.CREATE, MarketAdjustEntity.PASS)) {
			throw new AMPException("该日已进行市值校准, 不可重复操作.");
		}
		if (null == adjust) {
			// 上一次数据

			adjust = new MarketAdjustEntity();
			adjust.setOid(StringUtil.uuid());
			adjust.setAssetPool(this.assetPoolService.getByOid(pid));
			adjust.setStatus(MarketAdjustEntity.INIT);
		}
		MarketAdjustEntity lastMarket = adjustDao.getMaxBaseDateByPid(pid);
		adjust.setLastBaseDate(null != lastMarket ? lastMarket.getBaseDate() : null);
		adjust.setLastShares(null != lastMarket ? lastMarket.getShares() : BigDecimal.ZERO);
		adjust.setLastNav(null != lastMarket ? lastMarket.getNav() : BigDecimal.ZERO);
		adjust.setBaseDate(baseDate);

		List<SPVOrder> objList = this.spvOrderService.getListForMarketAdjust(pid, baseDate);
		BigDecimal puchaseAmount = BigDecimal.ZERO;
		BigDecimal redeemAmount = BigDecimal.ZERO;
		BigDecimal lastOrders = BigDecimal.ZERO;
		if (null != objList && !objList.isEmpty()) {

			// 记录销售订单oid
			List<String> oidList = Lists.newArrayList();
			for (SPVOrder obj : objList) {
				if ("REDEEM".equals(obj.getOrderType())) {
					redeemAmount = redeemAmount.add(obj.getOrderAmount()).setScale(4, BigDecimal.ROUND_HALF_UP);
				} else {
					puchaseAmount = puchaseAmount.add(obj.getOrderAmount()).setScale(4, BigDecimal.ROUND_HALF_UP);
				}
				oidList.add(obj.getOid());
			}
			lastOrders = puchaseAmount.subtract(redeemAmount).setScale(4, BigDecimal.ROUND_HALF_UP);
		}

		adjust.setPurchase(puchaseAmount);
		adjust.setRedemption(redeemAmount);
		adjust.setLastOrders(lastOrders);
		adjust = this.adjustDao.save(adjust);
		boolean edit = this.marketAdjustOrderService.save(adjust, objList);
		if (edit || StringUtil.isEmpty(adjust.getAversion())) {
			String aversion = String.valueOf(System.currentTimeMillis());
			adjust.setAversion(aversion);
			this.adjustDao.save(adjust);
		}

		return new MarketAdjustResp(adjust);
	}

	/**
	 * 获取市值校准的基准日
	 * 当日15点以前的，算T-2日的订单投资
	 * 当日15点以后的，算T-1日的订单投资
	 * 
	 * @return
	 */
	public Date getBaseDate() {

		Calendar cutTime = Calendar.getInstance();
		cutTime.set(Calendar.HOUR_OF_DAY, 15);
		cutTime.set(Calendar.MINUTE, 0);
		cutTime.set(Calendar.SECOND, 0);
		cutTime.set(Calendar.MILLISECOND, 0);

		Calendar c = Calendar.getInstance();
		if (c.getTimeInMillis() < cutTime.getTimeInMillis()) {
			c.add(Calendar.DATE, -1);
		}

		return new Date(c.getTimeInMillis());

	}

	@Transactional
	public void delete(String oid, String operator) {

		MarketAdjustEntity adjust = this.findOne(oid);

		boolean next = false;

		if (adjust.getStatus().equals(MarketAdjustEntity.FAIL)) {
			next = true;
		}
		if (adjust.getStatus().equals(MarketAdjustEntity.CREATE)) {
			Date baseDate = this.getBaseDate();
			if (!DateUtil.same(baseDate, adjust.getBaseDate())) {
				next = true;
			}
		}
		if (next) {
			adjust.setStatus(MarketAdjustEntity.DELETE);
			this.adjustDao.save(adjust);
		} else {
			throw new AMPException("该订单不可删除!");
		}
	}

}
