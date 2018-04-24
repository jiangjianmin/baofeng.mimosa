package com.guohuai.ams.cashtool.pool;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.cashtool.CashTool;
import com.guohuai.ams.cashtool.CashToolDao;
import com.guohuai.ams.cashtool.log.CashToolLogService;
import com.guohuai.ams.enums.CashToolEventType;
import com.guohuai.component.exception.AMPException;

/**
 * 
 * <p>
 * Title: CashtoolPoolService.java
 * </p>
 * <p>
 * 现金管理工具备选库Service
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年5月18日 下午3:31:20
 */
@Service
@Transactional
public class CashtoolPoolService {
	@Autowired
	private CashtoolRevenueDao cashtoolRevenueDao;

	@Autowired
	private CashToolDao cashtoolDao;

	@Autowired
	private CashToolLogService cashtoolLogservice;

	/**
	 * 查询指定类型的所有现金管理类工具
	 * 
	 * @Title: getCollecting
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param cashtoolTypes
	 * @return List<CashTool> 返回类型
	 */
	public List<CashTool> getCollecting(final String[] cashtoolTypes) {
		Specification<CashTool> spec = new Specification<CashTool>() {

			@Override
			public Predicate toPredicate(Root<CashTool> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				if (null != cashtoolTypes && cashtoolTypes.length > 0) {
					Expression<String> expType = root.get("etfLof").as(String.class); // 现金管理工具类型
					predicate.add(expType.in(cashtoolTypes));// type = PREPARE
				}

				Expression<String> exp = root.get("state").as(String.class); // 现金管理工具状态
				predicate.add(exp.in(new Object[] { CashTool.CASHTOOL_STATE_collecting }));// state = PREPARE

				// Expression<Date> expHa = root.get("collectEndDate").as(Date.class); // 募集截止日
				// Predicate p = cb.lessThanOrEqualTo(expHa, collectEndDate); //募集截止日 <= 指定日期
				// predicate.add(p);

				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};
		return cashtoolDao.findAll(spec);
	}

	/**
	 * 现金管理工具收益采集
	 * 
	 * @Title: save
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param form
	 * @return CashToolRevenue 返回类型
	 */
	public CashToolRevenue cashToolRevenue(CashToolRevenueForm form) {
		String cashtoolOid = form.getCashtoolOid();
		if (StringUtils.isBlank(cashtoolOid))
			throw AMPException.getException("现金管理工具id不能为空");

		CashToolRevenue cr = new CashToolRevenue();
		BeanUtils.copyProperties(form, cr);
		BigDecimal weeklyYield = form.getWeeklyYield();
		// 七日化收益 百分比转小数
		if (weeklyYield != null) {
			weeklyYield = weeklyYield.divide(new BigDecimal(100));
			cr.setWeeklyYield(weeklyYield);
		}

		CashTool cashTool = this.cashtoolDao.findOne(cashtoolOid);
		if (null == cashTool)
			throw AMPException.getException("找不到id为[" + cashtoolOid + "]的现金管理工具");
		cr.setCashTool(cashTool);

		cr.setCreateTime(new Timestamp(System.currentTimeMillis()));
		cr.setUpdateTime(new Timestamp(System.currentTimeMillis()));

		this.cashtoolLogservice.saveCashToolLog(cashTool, CashToolEventType.revenue, form.getOperator()); // 现金管理工具收益采集

		this.cashtoolRevenueDao.deleteByDailyProfitDate(cashtoolOid, form.getDailyProfitDate());

		cr = cashtoolRevenueDao.save(cr);

		this.cashtoolDao.cashtoolRevenue(cashTool.getOid(), cr.getDailyProfitDate(), cr.getDailyProfit(), cr.getWeeklyYield());

		return cr;
	}

	/**
	 * 根据现金管理工具id和交易日期查询收益
	 * 
	 * @Title: findCashtoolRevenue
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param cashtoolOid
	 *            现金管理工具id
	 * @param dailyProfitDate
	 *            交易日期
	 * @return CashToolRevenue 返回类型
	 */
	public CashToolRevenue findCashtoolRevenue(String cashtoolOid, Date dailyProfitDate) {
		return cashtoolRevenueDao.findCashtoolRevenue(cashtoolOid, dailyProfitDate);
	}

	/**
	 * 根据现金管理工具id和交易日期查询收益
	 * 
	 * @Title: findCashtoolRevenue
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param cashtoolOids
	 *            现金管理工具id集合
	 * @param dailyProfitDate
	 *            交易日期
	 * @return Map<String,CashToolRevenue> key:现金管理工具id value:收益对象
	 */
	public Map<String, CashToolRevenue> findCashtoolRevenue(List<String> cashtoolOids, Date dailyProfitDate) {
		Map<String, CashToolRevenue> map = null;// key:现金管理工具id value:收益对象
		List<CashToolRevenue> list = cashtoolRevenueDao.findCashtoolRevenue(cashtoolOids, dailyProfitDate);
		int size = null == list ? 0 : list.size();
		if (size > 0) {
			map = new HashMap<>();
			for (CashToolRevenue cr : list) {
				map.put(cr.getCashTool().getOid(), cr);
			}
		}
		return map;
	}

}
